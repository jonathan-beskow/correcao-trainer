import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './app.component.html',
})
export class AppComponent {
  codigo: string = '';
  tipo: string = 'java';
  correcao: string = '';

  constructor(private http: HttpClient) {}

  sugerirCorrecao() {
    const body = { codigo: this.codigo, tipo: this.tipo };

    this.http.post<any>('http://localhost:8081/sugerir', body).subscribe({
      next: res => {
        this.correcao = res.correcoes || 'Nenhuma sugestão retornada.';
      },
      error: err => {
        this.correcao = 'Erro ao sugerir correção: ' + (err.message || err.statusText);
      }
    });
  }
}
