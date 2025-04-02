import { Routes } from '@angular/router';
import { CodeSuggestionComponent } from './components/code-suggestion.component';
import { CadastrarVulnerabilidadeComponent } from './components/cadastrar-vulnerabilidade/cadastrar-vulnerabilidade.component';
import { ListarApontamentosComponent } from './components/listar-apontamentos/listar-apontamentos.component';
import { GerarRelatorioComponent } from './gerar-relatorio/gerar-relatorio.component';

export const routes: Routes = [
  { path: '', component: CodeSuggestionComponent},
  { path: 'cadastro', component: CadastrarVulnerabilidadeComponent },
  { path: 'listar', component: ListarApontamentosComponent },
  { path: 'falsopositivo', component: GerarRelatorioComponent }
];

