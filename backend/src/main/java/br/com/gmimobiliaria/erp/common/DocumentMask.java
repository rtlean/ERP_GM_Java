package br.com.gmimobiliaria.erp.common;

/**
 * Mascara documentos pessoais em listagens (LGPD: minimização). CPF segue o padrão do governo ({@code ***.456.789-**});
 * CNPJ identifica empresa e é exibido formatado.
 */
public final class DocumentMask {
    private DocumentMask() {
    }

    public static String mask(String document) {
        if (document == null || document.isBlank()) {
            return null;
        }
        var digits = document.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return "***." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-**";
        }
        if (digits.length() == 14) {
            return digits.substring(0, 2) + "." + digits.substring(2, 5) + "." + digits.substring(5, 8) + "/"
                    + digits.substring(8, 12) + "-" + digits.substring(12);
        }
        var visible = Math.min(2, digits.length());
        return "*".repeat(Math.max(digits.length() - visible, 3)) + digits.substring(digits.length() - visible);
    }
}
