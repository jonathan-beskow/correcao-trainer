import { Routes } from '@angular/router';
import { CodeSuggestionComponent } from './components/code-suggestion.component';
import { CadastrarVulnerabilidadeComponent } from './components/cadastrar-vulnerabilidade/cadastrar-vulnerabilidade.component';
import { ListarApontamentosComponent } from './components/listar-apontamentos/listar-apontamentos.component';

export const routes: Routes = [
  { path: '', component: CodeSuggestionComponent},
  { path: 'cadastro', component: CadastrarVulnerabilidadeComponent },
  { path: 'listar', component: ListarApontamentosComponent }
];

