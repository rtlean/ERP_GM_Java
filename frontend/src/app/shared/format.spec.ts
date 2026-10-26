import { formatCents, repassLabel, safeReturnUrl } from './format';

describe('formatCents', () => {
  it('formata centavos em reais', () => {
    expect(formatCents(150000).replace(/\s/g, ' ')).toBe('R$ 1.500,00');
    expect(formatCents(98050).replace(/\s/g, ' ')).toBe('R$ 980,50');
  });

  it('mostra traço quando não há valor', () => {
    expect(formatCents(null)).toBe('—');
  });
});

describe('safeReturnUrl', () => {
  it('aceita caminhos internos', () => {
    expect(safeReturnUrl('/imoveis?q=rua')).toBe('/imoveis?q=rua');
  });

  it('recusa outros domínios e o próprio login', () => {
    expect(safeReturnUrl('https://exemplo.com')).toBe('/');
    expect(safeReturnUrl('//exemplo.com')).toBe('/');
    expect(safeReturnUrl('/\\exemplo.com')).toBe('/');
    expect(safeReturnUrl('/login?voltar=/x')).toBe('/');
    expect(safeReturnUrl(undefined)).toBe('/');
  });
});

describe('repassLabel', () => {
  it('exibe o valor gravado pelo sistema atual', () => {
    expect(repassLabel('TRANSFERÊNCIA BANCÁRIA')).toBe('TRANSFERÊNCIA BANCÁRIA');
    expect(repassLabel(null)).toBe('—');
  });
});
