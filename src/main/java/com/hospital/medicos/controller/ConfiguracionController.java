package com.hospital.medicos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class ConfiguracionController {

    private final Environment environment;

    @Value("${empresa.ambiente}")
    private String ambiente;

    @Value("${server.port}")
    private String puerto;

    @Value("${spring.datasource.url}")
    private String urlBaseDatos;

    public ConfiguracionController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/configuracion")
    public Map<String, String> obtenerConfiguracion() {
        String[] perfiles = environment.getActiveProfiles();
        String perfil = perfiles.length > 0 ? perfiles[0] : "default";

        Map<String, String> configuracion = new LinkedHashMap<>();
        configuracion.put("ambiente", ambiente);
        configuracion.put("perfil", perfil);
        configuracion.put("puerto", puerto);
        configuracion.put("baseDatos", obtenerNombreBaseDatos(urlBaseDatos));
        return configuracion;
    }

    private String obtenerNombreBaseDatos(String url) {
        int fin = url.indexOf('?');
        String urlSinParametros = fin >= 0 ? url.substring(0, fin) : url;
        return urlSinParametros.substring(urlSinParametros.lastIndexOf('/') + 1);
    }
}
