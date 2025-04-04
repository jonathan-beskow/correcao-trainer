import { AfterViewChecked, Component, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { Router, RouterLink } from '@angular/router';
import * as Prism from 'prismjs';

@Component({
  selector: 'app-code-suggestion',
  standalone: true,
  templateUrl: './code-suggestion.component.html',
  styleUrls: ['./code-suggestion.component.scss'],
  imports: [CommonModule, FormsModule, RouterLink]
})
export class CodeSuggestionComponent implements AfterViewChecked {
  codigo: string = '';
  tipo: string = '';
  linguagem: string = '';
  respostaCompleta: string | null = null;
  loading: boolean = false;
  timeoutMsg: string = '';
  similaridade: number | null = null;

  constructor(
    private apiService: ApiService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngAfterViewChecked(): void {
    if (isPlatformBrowser(this.platformId)) {
      Prism.highlightAll(); // Só executa se estiver no browser
    }
  }

  sugerirCorrecao() {
    const payload = {
      codigo: this.codigo,
      tipo: this.tipo,
      linguagem: 'java',
    };

    this.loading = true;
    this.timeoutMsg = '';
    this.respostaCompleta = null;

    const timeout = setTimeout(() => {
      this.loading = false;
      this.timeoutMsg = '⏰ Tempo excedido. O servidor demorou para responder.';
    }, 60000);

    this.apiService.sugerir(payload).subscribe({
      next: (res: any) => {
        clearTimeout(timeout);
        this.loading = false;
        this.similaridade = res?.similaridade ?? null;
        this.respostaCompleta = res?.codigoCorrigido || 'Nenhuma sugestão retornada.';
      },
      error: (err) => {
        clearTimeout(timeout);
        this.loading = false;
        this.timeoutMsg = '❌ Erro: ' + (err.message || 'Não foi possível conectar.');
      }
    });
  }

  copiarCodigo() {
    if (this.respostaCompleta) {
      navigator.clipboard.writeText(this.respostaCompleta).then(() => {
        alert('Código copiado para a área de transferência!');
      }).catch(() => {
        alert('Falha ao copiar o código.');
      });
    }
  }

  cadastrarCorrecao() {
    this.router.navigate(['/cadastro']);
  }
}
