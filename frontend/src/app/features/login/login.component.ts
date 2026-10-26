import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { apiErrorMessage } from '../../core/api-error';
import { safeReturnUrl } from '../../shared/format';

@Component({
  selector: 'app-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  template: `
    <main class="login">
      <section class="panel" aria-labelledby="login-title">
        <img src="brand-logo.svg" alt="GM Imobiliária" class="logo" width="180" height="60" />
        <h1 id="login-title">Entrar no ERP</h1>
        <p class="hint">Use o mesmo e-mail e senha do sistema atual.</p>

        @if (expirada()) {
          <p class="notice" role="status">Sua sessão expirou. Entre novamente para continuar.</p>
        }

        <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <label for="email">E-mail</label>
          <input id="email" type="email" formControlName="email" autocomplete="username" autofocus
                 [attr.aria-invalid]="showError('email')" aria-describedby="email-error" />
          @if (showError('email')) {
            <span id="email-error" class="field-error">Informe um e-mail válido.</span>
          }

          <label for="password">Senha</label>
          <input id="password" type="password" formControlName="password" autocomplete="current-password"
                 [attr.aria-invalid]="showError('password')" aria-describedby="password-error" />
          @if (showError('password')) {
            <span id="password-error" class="field-error">A senha deve ter no mínimo 6 caracteres.</span>
          }

          @if (error()) {
            <p class="form-error" role="alert">{{ error() }}</p>
          }

          <button type="submit" class="primary" [disabled]="submitting()">
            {{ submitting() ? 'Entrando…' : 'Entrar' }}
          </button>
        </form>
      </section>
      <p class="footer">GM Imobiliária · Novo ERP em homologação</p>
    </main>
  `,
  styles: `
    .login { min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; background: linear-gradient(160deg, var(--navy) 0%, var(--brand) 100%); }
    .panel { width: 100%; max-width: 400px; background: var(--surface); border-radius: 16px; padding: 36px 32px; box-shadow: 0 24px 60px rgb(10 31 63 / 35%); }
    .logo { display: block; height: 60px; width: auto; margin: 0 auto 20px; }
    h1 { margin: 0; font-size: 22px; text-align: center; }
    .hint { margin: 6px 0 24px; text-align: center; color: var(--muted); font-size: 14px; }
    .notice { background: #fff7ed; color: #9a3412; border: 1px solid #fed7aa; border-radius: 8px; padding: 10px 12px; font-size: 14px; }
    form { display: flex; flex-direction: column; }
    label { font-weight: 600; font-size: 14px; margin: 14px 0 6px; }
    .field-error { color: var(--danger); font-size: 13px; margin-top: 4px; }
    .form-error { color: var(--danger); background: #fef3f2; border: 1px solid #fecdca; border-radius: 8px; padding: 10px 12px; font-size: 14px; margin: 16px 0 0; }
    button.primary { margin-top: 24px; height: 44px; }
    .footer { color: rgb(255 255 255 / 70%); font-size: 13px; margin-top: 20px; }
  `,
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Parâmetros de URL ligados pelo roteador. */
  readonly voltar = input<string>();
  readonly expirada = input<string>();

  protected readonly submitting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected showError(field: 'email' | 'password'): boolean {
    const control = this.form.controls[field];
    return control.invalid && (control.touched || control.dirty);
  }

  protected async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    try {
      const { email, password } = this.form.getRawValue();
      await this.auth.login(email, password);
      await this.router.navigateByUrl(safeReturnUrl(this.voltar()));
    } catch (error) {
      this.error.set(apiErrorMessage(error, 'Não foi possível entrar. Tente novamente.'));
      this.form.controls.password.reset();
    } finally {
      this.submitting.set(false);
    }
  }
}
