import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (total() > 0) {
      <nav class="pagination" aria-label="Paginação">
        <span class="range">{{ first() }}–{{ last() }} de {{ total() }}</span>
        <button type="button" class="ghost" [disabled]="page() === 0" (click)="pageChange.emit(page() - 1)">
          Anterior
        </button>
        <span class="current" aria-current="page">Página {{ page() + 1 }} de {{ pages() }}</span>
        <button type="button" class="ghost" [disabled]="page() + 1 >= pages()" (click)="pageChange.emit(page() + 1)">
          Próxima
        </button>
      </nav>
    }
  `,
  styles: `
    .pagination { display: flex; align-items: center; justify-content: flex-end; gap: 12px; padding: 14px 4px 0; font-size: 14px; color: var(--muted); }
    .range { margin-right: auto; }
    .current { min-width: 120px; text-align: center; }
  `,
})
export class PaginationComponent {
  readonly page = input.required<number>();
  readonly size = input.required<number>();
  readonly total = input.required<number>();
  readonly pageChange = output<number>();

  protected readonly pages = computed(() => Math.max(1, Math.ceil(this.total() / this.size())));
  protected readonly first = computed(() => this.page() * this.size() + 1);
  protected readonly last = computed(() => Math.min(this.total(), (this.page() + 1) * this.size()));
}
