import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

/** Estados de carregamento, erro e vazio, iguais em todas as listagens. */
@Component({
  selector: 'app-list-state',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (error()) {
      <div class="state error" role="alert">
        <strong>{{ error() }}</strong>
        <button type="button" class="ghost" (click)="retry.emit()">Tentar novamente</button>
      </div>
    } @else if (loading() && empty()) {
      <div class="state" aria-live="polite">
        <span class="spinner" aria-hidden="true"></span> Carregando…
      </div>
    } @else if (empty()) {
      <div class="state">
        @if (searching()) {
          <strong>Nenhum resultado para “{{ searching() }}”.</strong>
          <span>Confira a grafia ou busque por outro termo.</span>
        } @else {
          <strong>{{ emptyTitle() }}</strong>
          <span>Os cadastros continuam sendo feitos no sistema atual durante a migração.</span>
        }
      </div>
    }
  `,
  styles: `
    .state { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 48px 16px; text-align: center; color: var(--muted); }
    .state strong { color: var(--text); font-weight: 600; }
    .state.error strong { color: var(--danger); }
    .spinner { width: 18px; height: 18px; border: 2px solid var(--line); border-top-color: var(--brand); border-radius: 50%; animation: spin 0.8s linear infinite; display: inline-block; }
    @keyframes spin { to { transform: rotate(360deg); } }
    @media (prefers-reduced-motion: reduce) { .spinner { animation: none; } }
  `,
})
export class ListStateComponent {
  readonly loading = input.required<boolean>();
  readonly error = input<string | null>(null);
  readonly empty = input.required<boolean>();
  readonly searching = input<string>('');
  readonly emptyTitle = input('Nenhum registro encontrado.');
  readonly retry = output<void>();
}
