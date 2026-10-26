import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <section class="card message">
      <h1>Página não encontrada</h1>
      <p>O endereço não existe ou este módulo ainda não foi migrado.</p>
      <a routerLink="/">Voltar ao início</a>
    </section>
  `,
})
export class NotFoundComponent {}
