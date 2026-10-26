import { DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, Subject, debounceTime, distinctUntilChanged, merge, map, switchMap, tap, catchError, of } from 'rxjs';
import { PageResult } from '../core/api.types';
import { apiErrorMessage } from '../core/api-error';

export interface ListParams {
  q: string;
  page: number;
  filter: string;
}

/**
 * Estado de uma listagem paginada com busca: carregando, erro, vazio e dados.
 * A busca espera o usuário parar de digitar; requisições antigas são canceladas.
 */
export class PagedList<T> {
  readonly items = signal<T[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(25);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  private params: ListParams = { q: '', page: 0, filter: '' };
  private readonly typed = new Subject<string>();
  private readonly immediate = new Subject<ListParams>();

  constructor(fetch: (params: ListParams) => Observable<PageResult<T>>) {
    const destroyRef = inject(DestroyRef);
    const debounced = this.typed.pipe(
      map((q) => q.trim()),
      debounceTime(300),
      distinctUntilChanged(),
      map((q) => ({ ...this.params, q, page: 0 })),
    );

    merge(debounced, this.immediate)
      .pipe(
        tap((params) => {
          this.params = params;
          this.loading.set(true);
          this.error.set(null);
        }),
        switchMap((params) =>
          fetch(params).pipe(
            catchError((error: unknown) => {
              this.error.set(apiErrorMessage(error, 'Não foi possível carregar a lista.'));
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(destroyRef),
      )
      .subscribe((result) => {
        this.loading.set(false);
        if (!result) return;
        this.items.set(result.items);
        this.total.set(result.total);
        this.page.set(result.page);
        this.size.set(result.size);
      });
  }

  load(): void {
    this.immediate.next({ ...this.params });
  }

  search(q: string): void {
    this.typed.next(q);
  }

  filter(filter: string): void {
    this.immediate.next({ ...this.params, filter, page: 0 });
  }

  goTo(page: number): void {
    this.immediate.next({ ...this.params, page });
  }

  get currentQuery(): string {
    return this.params.q;
  }
}
