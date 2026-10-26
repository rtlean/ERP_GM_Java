import { HttpErrorResponse } from '@angular/common/http';
import { apiErrorMessage } from './api-error';

describe('apiErrorMessage', () => {
  it('usa a mensagem do corpo de erro da API', () => {
    const error = new HttpErrorResponse({
      status: 401,
      error: { error: { code: 'INVALID_CREDENTIALS', message: 'Credenciais inválidas ou expiradas.', details: [], requestId: 'x' } },
    });
    expect(apiErrorMessage(error)).toBe('Credenciais inválidas ou expiradas.');
  });

  it('explica falta de conexão', () => {
    expect(apiErrorMessage(new HttpErrorResponse({ status: 0 }))).toContain('Sem conexão');
  });

  it('cai no texto padrão sem corpo conhecido', () => {
    expect(apiErrorMessage(new Error('x'), 'Padrão')).toBe('Padrão');
  });
});
