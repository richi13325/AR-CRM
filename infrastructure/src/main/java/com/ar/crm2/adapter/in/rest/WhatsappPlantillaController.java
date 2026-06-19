package com.ar.crm2.adapter.in.rest;

import com.ar.crm2.whatsapp.application.plantilla.port.out.PlantillaPort;
import com.ar.crm2.whatsapp.domain.entity.Plantilla;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wa/plantillas")
@RequiredArgsConstructor
public class WhatsappPlantillaController {

    private final PlantillaPort plantillaPort;

    public record PlantillaDto(String id, String titulo, String contenido) {
        static PlantillaDto from(Plantilla p) {
            return new PlantillaDto(p.id().toString(), p.titulo(), p.contenido());
        }
    }

    public record CrearPlantillaReq(@NotBlank String titulo, @NotBlank String contenido) {}

    @GetMapping("/get-all")
    public ResponseEntity<List<PlantillaDto>> getAll() {
        return ResponseEntity.ok(plantillaPort.findAll().stream().map(PlantillaDto::from).toList());
    }

    @PostMapping("/create")
    public ResponseEntity<PlantillaDto> create(@Valid @RequestBody CrearPlantillaReq req) {
        Plantilla saved = plantillaPort.save(Plantilla.create(req.titulo(), req.contenido()));
        return ResponseEntity.ok(PlantillaDto.from(saved));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam UUID id) {
        plantillaPort.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
