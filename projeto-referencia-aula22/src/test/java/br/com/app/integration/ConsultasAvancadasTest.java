package br.com.app.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import br.com.app.dto.TurmaResumoDTO;
import br.com.app.entity.Estudante;
import br.com.app.entity.Professor;
import br.com.app.entity.Turma;
import br.com.app.entity.Turno;
import br.com.app.repository.EstudanteRepository;
import br.com.app.repository.ProfessorRepository;
import br.com.app.repository.TurmaRepository;
import br.com.app.service.EstudanteService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

// @Transactional no teste: tudo o que ele grava sofre rollback no final.
// A propriedade generate_statistics liga as estatísticas do Hibernate (para contar SELECTs).
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Transactional
class ConsultasAvancadasTest {

	@Autowired
	private TurmaRepository turmaRepo;
	@Autowired
	private ProfessorRepository professorRepo;
	@Autowired
	private EstudanteRepository estudanteRepo;
	@Autowired
	private EstudanteService estudanteService;
	@Autowired
	private EntityManager em;
	@Autowired
	private EntityManagerFactory emf;

	private Professor novoProfessor(String nome) {
		Professor professor = new Professor();
		professor.setNome(nome);
		professor.setEspecializacao("Teste");
		professor.setTelefone("0000");
		return professorRepo.save(professor);
	}

	private Turma novaTurma(String numero, Turno turno, Professor professor) {
		Turma turma = new Turma();
		turma.setNumero(numero);
		turma.setTitulo("Turma " + numero);
		turma.setTurno(turno);
		turma.setProfessor(professor);
		return turmaRepo.save(turma);
	}

	private Estudante novoEstudante(String nome, String email, LocalDate nascimento) {
		Estudante estudante = new Estudante();
		estudante.setNome(nome);
		estudante.setEmail(email);
		estudante.setDataNascimento(nascimento);
		return estudanteRepo.save(estudante);
	}

	@Test
	void consultasDerivadasDoNomeDoMetodo() {
		Professor marta = novoProfessor("Marta Teste");
		Turma tarde = novaTurma("20991001", Turno.TARDE, marta);
		novaTurma("20991002", Turno.NOITE, marta);

		assertThat(turmaRepo.findByTurno(Turno.TARDE)).contains(tarde);
		assertThat(turmaRepo.findByProfessorMatricula(marta.getMatricula())).hasSize(2);
	}

	@Test
	void consultaJpqlComParametroNomeado() {
		novoEstudante("Carla Dominio", "carla@dominio-teste.com", LocalDate.of(2000, 1, 1));
		novoEstudante("Outro Dominio", "outro@outro-teste.com", LocalDate.of(2000, 1, 1));

		List<Estudante> encontrados = estudanteRepo.buscarPorDominioDoEmail("dominio-teste.com");

		assertThat(encontrados).extracting(Estudante::getNome).containsExactly("Carla Dominio");
	}

	@Test
	void filtrosDinamicosCombinamApenasOsParametrosInformados() {
		Professor marta = novoProfessor("Marta Filtro");
		Turma turma = novaTurma("20991003", Turno.MANHA, marta);
		Estudante ana = novoEstudante("Ana Filtro", "ana@filtro.com", LocalDate.of(2003, 5, 1));
		Estudante bia = novoEstudante("Bia Filtro", "bia@filtro.com", LocalDate.of(1990, 5, 1));
		turma.getEstudantes().add(ana);
		turma.getEstudantes().add(bia);
		em.flush();
		PageRequest page = PageRequest.of(0, 10);

		// só o nome
		Page<Estudante> porNome = estudanteService.buscarComFiltros("filtro", null, null, page);
		assertThat(porNome.getContent()).containsExactlyInAnyOrder(ana, bia);

		// nome + data de nascimento
		Page<Estudante> porNomeEData = estudanteService
				.buscarComFiltros("filtro", LocalDate.of(2000, 1, 1), null, page);
		assertThat(porNomeEData.getContent()).containsExactly(ana);

		// turma + nome (usa JOIN com a tabela de ligação)
		Page<Estudante> porTurma = estudanteService
				.buscarComFiltros("bia", null, turma.getId(), page);
		assertThat(porTurma.getContent()).containsExactly(bia);

		// nenhum filtro: devolve todos (pelo menos os criados aqui)
		assertThat(estudanteService.buscarComFiltros(null, null, null, page).getTotalElements())
				.isGreaterThanOrEqualTo(2);
	}

	@Test
	void relatorioComProjecaoEmRecord() {
		Professor marta = novoProfessor("Marta Relatorio");
		Turma turma = novaTurma("20991004", Turno.NOITE, marta);
		turma.getEstudantes().add(novoEstudante("R1", "r1@rel.com", LocalDate.of(2000, 1, 1)));
		turma.getEstudantes().add(novoEstudante("R2", "r2@rel.com", LocalDate.of(2000, 1, 1)));
		novaTurma("20991005", Turno.NOITE, marta); // turma vazia: total 0
		em.flush();

		List<TurmaResumoDTO> resumo = turmaRepo.resumoDasTurmas();

		assertThat(resumo).filteredOn(r -> r.numero().equals("20991004"))
				.singleElement()
				.satisfies(r -> {
					assertThat(r.professor()).isEqualTo("Marta Relatorio");
					assertThat(r.totalEstudantes()).isEqualTo(2L);
				});
		assertThat(resumo).filteredOn(r -> r.numero().equals("20991005"))
				.singleElement()
				.extracting(TurmaResumoDTO::totalEstudantes).isEqualTo(0L);
	}

	// Conta quantos SELECTs o Hibernate executou ao rodar a consulta e usar o professor de cada turma
	private long contarConsultas(Supplier<List<Turma>> consulta) {
		Statistics estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
		em.flush();
		em.clear(); // limpa o cache da sessão, para o Hibernate ir mesmo ao banco
		estatisticas.clear();

		consulta.get().forEach(turma -> {
			if (turma.getProfessor() != null) {
				turma.getProfessor().getNome(); // acessar o nome força o carregamento do professor
			}
		});
		return estatisticas.getPrepareStatementCount();
	}

	@Test
	void entityGraphEliminaOProblemaNMaisUm() {
		novaTurma("20991006", Turno.MANHA, novoProfessor("Prof A"));
		novaTurma("20991007", Turno.MANHA, novoProfessor("Prof B"));
		novaTurma("20991008", Turno.MANHA, novoProfessor("Prof C"));

		long semEntityGraph = contarConsultas(() -> turmaRepo.findAll());
		long comEntityGraph = contarConsultas(() -> turmaRepo.findAllComProfessor());

		System.out.println("SELECTs sem EntityGraph: " + semEntityGraph
				+ " | com EntityGraph: " + comEntityGraph);
		assertThat(comEntityGraph).isEqualTo(1);
		assertThat(semEntityGraph).isGreaterThanOrEqualTo(4); // 1 das turmas + 1 por professor
	}
}
