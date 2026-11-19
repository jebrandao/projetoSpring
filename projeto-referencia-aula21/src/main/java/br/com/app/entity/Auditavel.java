package br.com.app.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

// Superclasse mapeada: seus campos viram colunas de cada entidade que a estende.
// O AuditingEntityListener preenche as datas e os usuários automaticamente.
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditavel {

	@CreatedDate
	@Column(updatable = false)
	private LocalDateTime criadoEm;

	@LastModifiedDate
	private LocalDateTime atualizadoEm;

	@CreatedBy
	@Column(updatable = false)
	private String criadoPor;

	@LastModifiedBy
	private String atualizadoPor;
}
