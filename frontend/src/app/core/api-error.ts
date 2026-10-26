import { HttpErrorResponse } from '@angular/common/http';
import { ApiErrorBody } from './api.types';

/** Mensagem segura para exibir, a partir do corpo de erro padronizado da API. */
export function apiErrorMessage(error: unknown, fallback = 'Não foi possível concluir a operação. Tente novamente.'): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Sem conexão com o servidor. Verifique a internet e tente novamente.';
    }
    const body = error.error as Partial<ApiErrorBody> | null;
    const message = body?.error?.message;
    if (typeof message === 'string' && message.length > 0) {
      return message;
    }
  }
  return fallback;
}
