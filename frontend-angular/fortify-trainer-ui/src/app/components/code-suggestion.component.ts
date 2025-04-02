import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { Router, RouterLink } from '@angular/router';
import { Subscription, timeout } from 'rxjs';

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
  correcao: string = '';
  loading: boolean = false;
  timeoutId: any;
  timeoutMsg: string = '';
  codigoFormatado: string = '';


  constructor(private apiService: ApiService, private router: Router) {}

  copiarParaClipboard() {
    navigator.clipboard.writeText(this.codigoFormatado).then(() => {
      alert('Código copiado para a área de transferência!');
    }).catch(() => {
      alert('Erro ao copiar o conteúdo.');
    });
  }

  sugerirCorrecao() {
    const payload = {
      codigo: this.codigo,
      tipo: this.tipo,
      linguagem: "java",
    };

    this.loading = true;

    const timeout = setTimeout(() => {
      this.loading = false;
      this.timeoutMsg = '⏰ Tempo excedido. O servidor demorou para responder.';
    }, 60000);

    this.apiService.sugerir(payload).subscribe({
      next: (res: any) => {
        clearTimeout(timeout);
        this.loading = false;
        this.correcao = res.codigoCorrigido || 'Nenhuma sugestão retornada.';

        // 👇 separa o código e armazena para copiar depois
        const partes = this.correcao.split('```');
        this.codigoFormatado = partes[1]?.trim() || '';
      },
      error: (err) => {
        clearTimeout(timeout);
        this.loading = false;
        this.timeoutMsg = '❌ Erro: ' + (err.message || 'Não foi possível conectar.');
      }
    });
  }


  formatarCodigoCompleto(texto: string): { justificativa: string, codigo: string } {
    const partes = texto.split('```');
    const justificativa = partes[0]?.trim() || '';
    const codigo = partes[1]?.trim() || 'Não foi possível extrair o código.';
    return { justificativa, codigo };
  }



  cadastrarCorrecao() {
    this.router.navigate(['/cadastro']);
  }
}
