import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListarApontamentosComponent } from './listar-apontamentos.component';

describe('ListarApontamentosComponent', () => {
  let component: ListarApontamentosComponent;
  let fixture: ComponentFixture<ListarApontamentosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListarApontamentosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListarApontamentosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
