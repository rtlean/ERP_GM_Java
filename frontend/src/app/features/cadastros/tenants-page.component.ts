import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { TenantSummary } from '../../core/api.types';
import { ListStateComponent } from '../../shared/list-state.component';
import { PagedList } from '../../shared/paged-list';
import { PaginationComponent } from '../../shared/pagination.component';
import { formatCents } from '../../shared/format';
import { CadastrosApi } from './cadastros.api';

@Component({
  selector: 'app-tenants-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ListStateComponent, PaginationComponent],
  template: `
    <header class="page-header">
      <div>
        <h1>Locatários</h1>
        <p class="subtitle">Inquilinos e seus locadores pelos contratos ativos. Consulta somente leitura nesta fase.</p>
      </div>
    </header>

    <section class="card">
      <div class="toolbar">
        <input type="search" class="search" placeholder="Buscar por nome ou e-mail" aria-label="Buscar locatários"
               (input)="list.search($any($event.target).value)" />
      </div>

      @if (list.items().length > 0) {
        <div class="table-wrap" [class.refreshing]="list.loading()">
          <table>
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">CPF/CNPJ</th>
                <th scope="col">Contato</th>
                <th scope="col">Locador(es)</th>
                <th scope="col" class="num">Aluguel</th>
              </tr>
            </thead>
            <tbody>
              @for (tenant of list.items(); track tenant.id) {
                <tr>
                  <td class="strong">
                    {{ tenant.fullName }}
                    @if (tenant.propertyType) { <div class="muted">{{ tenant.propertyType }}</div> }
                  </td>
                  <td class="mono">{{ tenant.documentMasked ?? '—' }}</td>
                  <td>
                    <div>{{ tenant.email ?? '—' }}</div>
                    @if (tenant.phone) { <div class="muted">{{ tenant.phone }}</div> }
                  </td>
                  <td>{{ tenant.ownerNames.length ? tenant.ownerNames.join(', ') : '—' }}</td>
                  <td class="num">{{ money(tenant.rentCents) }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <app-list-state [loading]="list.loading()" [error]="list.error()" [empty]="list.items().length === 0"
                      [searching]="list.currentQuery" emptyTitle="Nenhum locatário cadastrado." (retry)="list.load()" />
      <app-pagination [page]="list.page()" [size]="list.size()" [total]="list.total()" (pageChange)="list.goTo($event)" />
    </section>
  `,
})
export class TenantsPageComponent implements OnInit {
  private readonly api = inject(CadastrosApi);
  protected readonly list = new PagedList<TenantSummary>((params) => this.api.tenants(params));
  protected readonly money = formatCents;

  ngOnInit(): void {
    this.list.load();
  }
}
