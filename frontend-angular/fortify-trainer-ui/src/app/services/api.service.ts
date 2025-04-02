import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Apontamento } from '../models/apontamento.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  sugerir(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/sugerir-correcao/corrigir`, payload);
  }

}