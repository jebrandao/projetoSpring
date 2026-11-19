package br.com.app.inicializacao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.app.entity.Estudante;
import br.com.app.entity.Professor;
import br.com.app.entity.Turma;
import br.com.app.entity.Turno;
import br.com.app.service.EstudanteService;
import br.com.app.service.ProfessorService;
import br.com.app.service.TurmaService;
import lombok.RequiredArgsConstructor;

// O ApplicationRunner executa uma única vez, quando a aplicação termina de subir
@Component
@RequiredArgsConstructor
public class Init implements ApplicationRunner {

	private final EstudanteService estudanteService;
	private final ProfessorService professorService;
	private final TurmaService turmaService;

	@Override
	public void run(ApplicationArguments args) {
		// Evita duplicar os dados a cada reinício (o MySQL mantém os dados)
		if (!estudanteService.buscarTodosEstudantes().isEmpty()) {
			return;
		}

		// ---------- Estudantes
		Estudante estudante1 = novoEstudante("Alberto", "Peixoto",
				"albertop@email.com.br", LocalDate.of(1999, 11, 5));
		estudante1 = estudanteService.salvar(estudante1);

		Estudante estudante2 = novoEstudante("Cremilda", "Viana",
				"cviana@email.com.br", LocalDate.of(1975, 9, 4));
		estudanteService.salvar(estudante2);

		Estudante estudante3 = novoEstudante("Bernardo", "Silva",
				"bsilva@email.com.br", LocalDate.of(2005, 6, 14));
		estudante3 = estudanteService.salvar(estudante3);

		// buscar todos os estudantes
		List<Estudante> lista = estudanteService.buscarTodosEstudantes();
		lista.forEach(estudante -> System.out.println(estudante.getNome()));

		// buscar um estudante pelo id
		Estudante encontrado = estudanteService
				.buscarEstudantePorId(estudante1.getId());
		System.out.println(encontrado.getNome());

		// alterar estudante
		Estudante estudante4 = novoEstudante("Bernardo", "Silva e Santos",
				"bss@email.com.br", LocalDate.of(2005, 6, 14));
		Estudante alterado = estudanteService
				.updateEstudante(estudante3.getId(), estudante4);
		System.out.println(alterado.getEmail());

		// ---------- Professores
		professorService.salvar(novoProfessor("Marta Oliveira Santos",
				"Banco de Dados", "(21) 99999-0000"));
		professorService.salvar(novoProfessor("Paulo Ricardo Lima",
				"Programação Java", "(21) 98888-1111"));

		// ---------- Turmas
		turmaService.salvar(novaTurma("20240007",
				"Desenvolvimento de Sistemas", Turno.NOITE));
		turmaService.salvar(novaTurma("20240008",
				"Análise de Dados", Turno.MANHA));
	}

	private Estudante novoEstudante(String nome, String sobreNome,
			String email, LocalDate nascimento) {
		Estudante estudante = new Estudante();
		estudante.setNome(nome);
		estudante.setSobreNome(sobreNome);
		estudante.setEmail(email);
		estudante.setDataNascimento(nascimento);
		return estudante;
	}

	private Professor novoProfessor(String nome, String especializacao,
			String telefone) {
		Professor professor = new Professor();
		professor.setNome(nome);
		professor.setEspecializacao(especializacao);
		professor.setTelefone(telefone);
		return professor;
	}

	private Turma novaTurma(String numero, String titulo, Turno turno) {
		Turma turma = new Turma();
		turma.setNumero(numero);
		turma.setTitulo(titulo);
		turma.setTurno(turno);
		return turma;
	}
}
