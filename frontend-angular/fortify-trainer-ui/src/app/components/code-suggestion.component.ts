import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../services/api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-code-suggestion',
  standalone: true,
  templateUrl: './code-suggestion.component.html',
  styleUrls: ['./code-suggestion.component.scss'],
  imports: [CommonModule, FormsModule]
})
export class CodeSuggestionComponent {
  codigo: string = '';
  tipo: string = 'SQL Injection';
  linguagem: string = 'Java';
  contexto: string = 'UserRepository.java -> findByEmail';
  correcao: string = '';

  constructor(private apiService: ApiService, private router: Router) {}

  sugerirCorrecao() {
    const payload = {
      codigo: this.codigo,
      tipo: this.tipo,
      linguagem: "java",
    };
  
    this.apiService.sugerir(payload).subscribe({
      next: (res: any) => {
        this.correcao = res.codigoCorrigido || 'Nenhuma sugestão retornada.';
      },
      error: (err) => {
        this.correcao = 'Erro: ' + (err.message || 'Não foi possível conectar.');
      }
    });
  }
  

  cadastrarCorrecao() {
    this.router.navigate(['/cadastro']);
  }
}

