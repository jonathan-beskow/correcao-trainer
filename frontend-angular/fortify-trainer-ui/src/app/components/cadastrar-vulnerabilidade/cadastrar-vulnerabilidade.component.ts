import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cadastrar-vulnerabilidade',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cadastrar-vulnerabilidade.component.html',
  styleUrls: ['./cadastrar-vulnerabilidade.component.scss']
})
export class CadastrarVulnerabilidadeComponent {
  nomeApontamento: string = '';
  tipo: string = '';
  codigoOriginal: string = '';
  codigoCorrigido: string = '';

  constructor(private http: HttpClient, private router: Router) {}

  voltar() {
    this.router.navigate(['/']);
  }

  cadastrarCaso() {
    const payload = {
      tipo: this.tipo,
      linguagem: 'Java',
      codigoOriginal: this.codigoOriginal,
      codigoCorrigido: this.codigoCorrigido,
      justificativa: 'Cadastro manual do usuário.'
    };

    this.http.post('http://localhost:8081/sugerir-correcao/cadastrar-caso', payload).subscribe({
      next: (res: any) => {
        alert('✅ Vulnerabilidade cadastrada com sucesso!');
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Erro ao cadastrar:', err);
        alert(`❌ Erro ao cadastrar: ${err.status} - ${err.statusText}`);
      }
    });
  }
}
