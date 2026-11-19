package br.com.app.inicializacao;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.app.entity.Endereco;
import br.com.app.entity.Estudante;
import br.com.app.entity.Professor;
import br.com.app.entity.Turma;
import br.com.app.entity.Turno;
import br.com.app.service.EstudanteService;
import br.com.app.service.ProfessorService;
import br.com.app.service.TurmaService;
import br.com.app.service.UsuarioService;
import lombok.RequiredArgsConstructor;

// O ApplicationRunner executa uma única vez, quando a aplicação termina de subir
@Component
@RequiredArgsConstructor
public class Init implements ApplicationRunner {

	private final EstudanteService estudanteService;
	private final ProfessorService professorService;
	private final TurmaService turmaService;
	private final UsuarioService usuarioService;

	@Override
	public void run(ApplicationArguments args) {
		criarUsuarios();

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
		estudante2 = estudanteService.salvar(estudante2);

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
		Professor professor1 = novoProfessor("Marta Oliveira Santos",
				"Banco de Dados", "(21) 99999-0000");
		professor1 = professorService.salvar(professor1);

		Professor professor2 = novoProfessor("Paulo Ricardo Lima",
				"Programação Java", "(21) 98888-1111");
		professor2 = professorService.salvar(professor2);

		// ---------- Turmas (N:1 com professor e N:N com estudantes)
		Turma turma1 = novaTurma("20240007", "Desenvolvimento de Sistemas",
				Turno.NOITE, professor1);
		turma1 = turmaService.salvar(turma1);

		Turma turma2 = novaTurma("20240008", "Análise de Dados",
				Turno.MANHA, professor2);
		turma2 = turmaService.salvar(turma2);

		turmaService.matricularEstudante(turma1.getId(), estudante1.getId());
		turmaService.matricularEstudante(turma1.getId(), estudante2.getId());
		turmaService.matricularEstudante(turma2.getId(), estudante2.getId());
	}

	// Usuários de teste (apenas didático: em produção use senhas fortes
	// vindas de variáveis de ambiente)
	private void criarUsuarios() {
		if (!usuarioService.existe("admin")) {
			usuarioService.criarUsuario("admin", "admin123", Set.of("ADMIN"));
		}
		if (!usuarioService.existe("aluno")) {
			usuarioService.criarUsuario("aluno", "aluno123", Set.of("USER"));
		}
	}

	private Estudante novoEstudante(String nome, String sobreNome,
			String email, LocalDate nascimento) {
		Estudante estudante = new Estudante();
		estudante.setNome(nome);
		estudante.setSobreNome(sobreNome);
		estudante.setEmail(email);
		estudante.setDataNascimento(nascimento);

		Endereco endereco = new Endereco();
		endereco.setLogradouro("Rua das Flores");
		endereco.setNumero("100");
		endereco.setCep("20000-000");
		endereco.setCidade("Rio de Janeiro");
		estudante.setEndereco(endereco);
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

	private Turma novaTurma(String numero, String titulo, Turno turno,
			Professor professor) {
		Turma turma = new Turma();
		turma.setNumero(numero);
		turma.setTitulo(titulo);
		turma.setTurno(turno);
		turma.setProfessor(professor);
		return turma;
	}
}
