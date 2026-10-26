import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { OwnerSummary, PageResult, PropertySummary, TenantSummary } from '../../core/api.types';
import { ListParams } from '../../shared/paged-list';

@Injectable({ providedIn: 'root' })
export class CadastrosApi {
  private readonly http = inject(HttpClient);

  owners(params: ListParams): Observable<PageResult<OwnerSummary>> {
    return this.http.get<PageResult<OwnerSummary>>('/api/v1/owners', { params: toHttp(params) });
  }

  properties(params: ListParams): Observable<PageResult<PropertySummary>> {
    const http = params.filter ? toHttp(params).set('status', params.filter) : toHttp(params);
    return this.http.get<PageResult<PropertySummary>>('/api/v1/properties', { params: http });
  }

  tenants(params: ListParams): Observable<PageResult<TenantSummary>> {
    return this.http.get<PageResult<TenantSummary>>('/api/v1/tenants', { params: toHttp(params) });
  }
}

function toHttp(params: ListParams): HttpParams {
  let http = new HttpParams().set('page', params.page);
  if (params.q) {
    http = http.set('q', params.q);
  }
  return http;
}
