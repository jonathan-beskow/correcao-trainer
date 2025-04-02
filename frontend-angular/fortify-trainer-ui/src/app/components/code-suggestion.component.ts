import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-code-suggestion',
  standalone: true,
  templateUrl: './code-suggestion.component.html',
  styleUrls: ['./code-suggestion.component.scss'],
  imports: [CommonModule, FormsModule, RouterLink]
})
export class CodeSuggestionComponent {
  codigo: string = '';
  tipo: string = '';
  linguagem: string = '';
  respostaCompleta: string | null = null;
  loading: boolean = false;
  timeoutMsg: string = '';
  similaridade: number | null = null;


  constructor(private apiService: ApiService, private router: Router) {}

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

  cadastrarCorrecao() {
    this.router.navigate(['/cadastro']);
  }
}
