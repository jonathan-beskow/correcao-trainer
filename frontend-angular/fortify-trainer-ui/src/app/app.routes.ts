import { Routes } from '@angular/router';
import { CodeSuggestionComponent } from './components/code-suggestion.component';
import { CadastrarVulnerabilidadeComponent } from './components/cadastrar-vulnerabilidade/cadastrar-vulnerabilidade.component';

export const routes: Routes = [
  { path: '', component: CodeSuggestionComponent},
  { path: 'cadastro', component: CadastrarVulnerabilidadeComponent }
];

