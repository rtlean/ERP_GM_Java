import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { OccupancyStatus, PropertySummary } from '../../core/api.types';
import { ListStateComponent } from '../../shared/list-state.component';
import { PagedList } from '../../shared/paged-list';
import { PaginationComponent } from '../../shared/pagination.component';
import { OCCUPANCY_LABELS, formatCents } from '../../shared/format';
import { CadastrosApi } from './cadastros.api';

@Component({
  selector: 'app-properties-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ListStateComponent, PaginationComponent],
  template: `
    <header class="page-header">
      <div>
        <h1>Imóveis</h1>
        <p class="subtitle">Imóveis administrados, com o locatário do contrato ativo. Consulta somente leitura nesta fase.</p>
      </div>
    </header>

    <section class="card">
      <div class="toolbar">
        <input type="search" class="search" placeholder="Buscar por código, endereço, cidade ou locador"
               aria-label="Buscar imóveis" (input)="list.search($any($event.target).value)" />
        <select aria-label="Filtrar por situação" (change)="list.filter($any($event.target).value)">
          <option value="">Todas as situações</option>
          @for (status of statuses; track status) {
            <option [value]="status">{{ labels[status] }}</option>
          }
        </select>
      </div>

      @if (list.items().length > 0) {
        <div class="table-wrap" [class.refreshing]="list.loading()">
          <table>
            <thead>
              <tr>
                <th scope="col">Código</th>
                <th scope="col">Endereço</th>
                <th scope="col">Locador</th>
                <th scope="col">Locatário</th>
                <th scope="col">Situação</th>
                <th scope="col" class="num">Aluguel</th>
              </tr>
            </thead>
            <tbody>
              @for (property of list.items(); track property.id) {
                <tr>
                  <td class="strong mono">{{ property.code }}</td>
                  <td>
                    <div>{{ property.address }}</div>
                    <div class="muted">{{ property.type }} · {{ property.city }}/{{ property.state }}</div>
                  </td>
                  <td>{{ property.ownerName ?? '—' }}</td>
                  <td>{{ property.tenantName ?? '—' }}</td>
                  <td><span class="badge" [class]="property.occupancyStatus">{{ labels[property.occupancyStatus] }}</span></td>
                  <td class="num">{{ money(property.rentCents) }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <app-list-state [loading]="list.loading()" [error]="list.error()" [empty]="list.items().length === 0"
                      [searching]="list.currentQuery" emptyTitle="Nenhum imóvel encontrado." (retry)="list.load()" />
      <app-pagination [page]="list.page()" [size]="list.size()" [total]="list.total()" (pageChange)="list.goTo($event)" />
    </section>
  `,
})
export class PropertiesPageComponent implements OnInit {
  private readonly api = inject(CadastrosApi);
  protected readonly list = new PagedList<PropertySummary>((params) => this.api.properties(params));
  protected readonly labels = OCCUPANCY_LABELS;
  protected readonly statuses = Object.keys(OCCUPANCY_LABELS) as OccupancyStatus[];
  protected readonly money = formatCents;

  ngOnInit(): void {
    this.list.load();
  }
}
