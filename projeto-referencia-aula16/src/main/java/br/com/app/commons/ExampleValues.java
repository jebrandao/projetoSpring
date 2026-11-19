package br.com.app.commons;

public class ExampleValues {

	// Classe utilitária: não deve ser instanciada
	private ExampleValues() {
	}

	// ---- Estudante
	public static final String EXEMPLO_ESTUDANTE = """
			{
			  "nome": "Zoroastra",
			  "sobreNome": "Pereira",
			  "email": "zoroastra@gmail.com",
			  "dataNascimento": "2001-03-15"
			}""";

	// ---- Professor
	public static final String EXEMPLO_PROFESSOR = """
			{
			  "nome": "Marta Oliveira Santos",
			  "especializacao": "Banco de Dados",
			  "telefone": "(21) 99999-0000"
			}""";

	// ---- Turma
	public static final String EXEMPLO_TURMA = """
			{
			  "numero": "20240007",
			  "titulo": "Desenvolvimento de Sistemas",
			  "turno": "NOITE"
			}""";
}
