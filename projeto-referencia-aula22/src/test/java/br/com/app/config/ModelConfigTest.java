package br.com.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import br.com.app.dto.TurmaDTO;
import br.com.app.entity.Professor;
import br.com.app.entity.Turma;
import br.com.app.entity.Turno;

// Garante que o ModelMapper converte a matrícula do professor nos dois sentidos
class ModelConfigTest {

	private final ModelMapper mapper = new ModelConfig().modelMapper();

	@Test
	void deveConverterTurmaEmDTO() {
		Professor professor = new Professor();
		professor.setMatricula(7);
		Turma turma = new Turma();
		turma.setNumero("20240001");
		turma.setTurno(Turno.NOITE);
		turma.setProfessor(professor);

		TurmaDTO dto = mapper.map(turma, TurmaDTO.class);

		assertThat(dto.getProfessorMatricula()).isEqualTo(7);
		assertThat(dto.getTurno()).isEqualTo(Turno.NOITE);
	}

	@Test
	void deveConverterDTOEmTurma() {
		TurmaDTO dto = new TurmaDTO();
		dto.setNumero("20240001");
		dto.setProfessorMatricula(9);

		Turma turma = mapper.map(dto, Turma.class);

		assertThat(turma.getProfessor().getMatricula()).isEqualTo(9);
	}
}
