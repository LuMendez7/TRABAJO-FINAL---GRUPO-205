package com.bibliogest.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ejemplar")
@Getter
@Setter
@NoArgsConstructor
public class Ejemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejemplar")
    private Long idEjemplar;

    @ManyToOne
    @JoinColumn(name = "id_libro", nullable = false)
    private Libro libro;

    @Column(name = "codigo_inventario", nullable = false, unique = true, length = 50)
    private String codigoInventario;

    @Column(length = 120)
    private String ubicacion;

    @Column(name = "estado_fisico", length = 20)
    private String estadoFisico;

    @Column(nullable = false, length = 30)
    private String estado;

    @Transient
    @JsonIgnore
    private boolean estadoEnviado;

    @Column(nullable = false)
    private Boolean activo = true;

    @Transient
    @JsonIgnore
    private boolean activoEnviado;

    @JsonSetter("estado")
    public void setEstado(String estado) {
        this.estado = estado;
        this.estadoEnviado = true;
    }

    @JsonSetter("activo")
    public void setActivo(Boolean activo) {
        this.activo = activo;
        this.activoEnviado = true;
    }
}
