import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-listar-apontamentos',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './listar-apontamentos.component.html',
  styleUrls: ['./listar-apontamentos.component.scss']
})
export class ListarApontamentosComponent implements OnInit {
  apontamentos: any[] = [];
  loading: boolean = true;
  erro: string | null = null;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.http.get<any[]>('http://localhost:8081/estatisticas/tipos') // 🔧 Corrigido aqui
      .subscribe({
        next: (data) => {
          this.apontamentos = data;
          this.loading = false;
        },
        error: (err) => {
          this.erro = 'Erro ao carregar os apontamentos.';
          this.loading = false;
        }
      });
  }

  voltar() {
    this.router.navigate(['/']);
  }
}
