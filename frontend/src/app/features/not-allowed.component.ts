import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-not-allowed',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="card message">
      <h1>Sem permissão</h1>
      <p>Seu perfil não tem acesso a esta área. Se precisar dela, fale com o administrador.</p>
    </section>
  `,
})
export class NotAllowedComponent {}
