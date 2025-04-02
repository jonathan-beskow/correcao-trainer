import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-gerar-relatorio',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],  // Importando ReactiveFormsModule diretamente no componente standalone
  templateUrl: './gerar-relatorio.component.html',
  styleUrls: ['./gerar-relatorio.component.scss']
})
export class GerarRelatorioComponent implements OnInit {
  uploadForm!: FormGroup;  // Adicionando o operador '!' para indicar que será inicializado mais tarde
  loading: boolean = false;
  errorMessage: string | null = null;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    // Inicializa o formulário reativo
    this.uploadForm = new FormGroup({
      file: new FormControl(null, [Validators.required])
    });
  }

  // Lida com a mudança do arquivo
  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.uploadForm.patchValue({
        file: file
      });
    }
  }

  // Submete o formulário
  onSubmit(): void {
    if (this.uploadForm.invalid) {
      return;
    }

    const formData = new FormData();
    formData.append('file', this.uploadForm.get('file')?.value);

    this.loading = true;
    this.errorMessage = null;

    // Requisição POST para o backend
    this.http.post('http://localhost:8081/api/falsos-positivos/gerar-relatorio', formData, { responseType: 'blob' })
      .subscribe({
        next: (data: Blob) => {
          // Cria o link para download do arquivo gerado
          const blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' });
          const link = document.createElement('a');
          link.href = URL.createObjectURL(blob);
          link.download = 'relatorio_falsos_positivos.docx';
          link.click();
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = 'Erro ao gerar o relatório';
          this.loading = false;
        }
      });
  }
}
