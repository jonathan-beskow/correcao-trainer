import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadastrarVulnerabilidadeComponent } from './cadastrar-vulnerabilidade.component';

describe('CadastrarVulnerabilidadeComponent', () => {
  let component: CadastrarVulnerabilidadeComponent;
  let fixture: ComponentFixture<CadastrarVulnerabilidadeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CadastrarVulnerabilidadeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CadastrarVulnerabilidadeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
