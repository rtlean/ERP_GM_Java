import { OccupancyStatus } from '../core/api.types';

const BRL = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

/** Valores chegam em centavos inteiros (sem ponto flutuante na API). */
export function formatCents(cents: number | null | undefined): string {
  return cents == null ? '—' : BRL.format(cents / 100);
}

export const OCCUPANCY_LABELS: Record<OccupancyStatus, string> = {
  occupied: 'Ocupado',
  vacant: 'Vago',
  maintenance: 'Em manutenção',
};

/** O sistema atual grava a forma de repasse já como rótulo ("PIX", "TRANSFERÊNCIA BANCÁRIA"). */
export function repassLabel(method: string | null | undefined): string {
  return method?.trim() || '—';
}

/** Destino seguro após o login: só caminhos internos, nunca outro domínio. */
export function safeReturnUrl(value: string | null | undefined): string {
  if (!value || !value.startsWith('/') || value.startsWith('//') || value.startsWith('/\\')) {
    return '/';
  }
  return value.startsWith('/login') ? '/' : value;
}
