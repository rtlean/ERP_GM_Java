package br.com.gmimobiliaria.erp.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DocumentMaskTest {
    @Test
    void cpfMostraSoOMeio() {
        assertEquals("***.456.789-**", DocumentMask.mask("123.456.789-01"));
        assertEquals("***.456.789-**", DocumentMask.mask("12345678901"));
    }

    @Test
    void cnpjEFormatado() {
        assertEquals("12.345.678/0001-90", DocumentMask.mask("12345678000190"));
    }

    @Test
    void outrosFormatosMostramSoOFinal() {
        assertEquals("*******89", DocumentMask.mask("1234567-89"));
        assertEquals("***1", DocumentMask.mask("1"));
    }

    @Test
    void vazioViraNulo() {
        assertNull(DocumentMask.mask(null));
        assertNull(DocumentMask.mask("  "));
    }
}
