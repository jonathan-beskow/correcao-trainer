package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Promptutils {

    private static final Logger logger = LoggerFactory.getLogger(Promptutils.class);

    public static String generatePrompt(String tipo, String codigoOriginal, String codigoCorrigido, String codigoAlvo) {
        return String.format("""
        Corrija o seguinte código vulnerável aplicando uma abordagem semelhante à usada no exemplo anterior.

        Tipo de vulnerabilidade: %s

        Exemplo de código vulnerável:
        %s

        Correção aplicada:
        %s

        Código a ser corrigido:
        %s

        Código corrigido (retorne apenas o novo código, sem comentários ou explicações):
        """, tipo, codigoOriginal, codigoCorrigido, codigoAlvo);
    }


    public static String generatePrompt(String tipo, String codigoCorrigido, String codigoAlvo) {
        return String.format("""
        Dado um exemplo de correção de vulnerabilidade do tipo %s, aplique uma abordagem semelhante no código abaixo.

        Exemplo de correção:
        %s

        Código a ser corrigido:
        %s

        Código corrigido (retorne apenas o novo código):
        """, tipo, codigoCorrigido, codigoAlvo);
    }


    public static String generatePromptWithOutBase(String tipo, String codigoAlvo) {
        return String.format("""
        O código abaixo apresenta uma vulnerabilidade do tipo %s.
        Corrija-o da forma menos invasiva possível, mantendo a lógica e estrutura original.

        Código a ser corrigido:
        %s

        Código corrigido (somente o código, sem explicações):
        """, tipo, codigoAlvo);
    }
    // Teste

}
