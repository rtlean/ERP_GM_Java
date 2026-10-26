import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { OwnerSummary } from '../../core/api.types';
import { ListStateComponent } from '../../shared/list-state.component';
import { PagedList } from '../../shared/paged-list';
import { PaginationComponent } from '../../shared/pagination.component';
import { repassLabel } from '../../shared/format';
import { CadastrosApi } from './cadastros.api';

@Component({
  selector: 'app-owners-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ListStateComponent, PaginationComponent],
  template: `
    <header class="page-header">
      <div>
        <h1>Locadores</h1>
        <p class="subtitle">Proprietários dos imóveis administrados. Consulta somente leitura nesta fase.</p>
      </div>
    </header>

    <section class="card">
      <div class="toolbar">
        <input type="search" class="search" placeholder="Buscar por nome ou e-mail" aria-label="Buscar locadores"
               (input)="list.search($any($event.target).value)" />
      </div>

      @if (list.items().length > 0) {
        <div class="table-wrap" [class.refreshing]="list.loading()">
          <table>
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">Documento</th>
                <th scope="col">Contato</th>
                <th scope="col">Repasse</th>
                <th scope="col" class="num">Imóveis</th>
              </tr>
            </thead>
            <tbody>
              @for (owner of list.items(); track owner.id) {
                <tr>
                  <td class="strong">{{ owner.fullName }}</td>
                  <td class="mono">{{ owner.documentMasked ?? '—' }}</td>
                  <td>
                    <div>{{ owner.email ?? '—' }}</div>
                    @if (owner.phone) { <div class="muted">{{ owner.phone }}</div> }
                  </td>
                  <td>{{ repass(owner.preferredRepassMethod) }}</td>
                  <td class="num">{{ owner.propertiesCount }}</td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }

      <app-list-state [loading]="list.loading()" [error]="list.error()" [empty]="list.items().length === 0"
                      [searching]="list.currentQuery" emptyTitle="Nenhum locador cadastrado." (retry)="list.load()" />
      <app-pagination [page]="list.page()" [size]="list.size()" [total]="list.total()" (pageChange)="list.goTo($event)" />
    </section>
  `,
})
export class OwnersPageComponent implements OnInit {
  private readonly api = inject(CadastrosApi);
  protected readonly list = new PagedList<OwnerSummary>((params) => this.api.owners(params));
  protected readonly repass = repassLabel;

  ngOnInit(): void {
    this.list.load();
  }
}
