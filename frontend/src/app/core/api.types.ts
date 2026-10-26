/** Contratos da API Spring Boot (`/api/v1`). Ver docs/contratos-api.md. */

export type Permission =
  | 'USERS_READ' | 'USERS_WRITE'
  | 'PROPERTIES_READ' | 'PROPERTIES_WRITE'
  | 'PEOPLE_READ' | 'PEOPLE_WRITE'
  | 'CRM_READ' | 'CRM_WRITE'
  | 'CONTRACTS_READ' | 'CONTRACTS_WRITE'
  | 'FINANCE_READ' | 'FINANCE_WRITE'
  | 'DOCUMENTS_READ' | 'DOCUMENTS_WRITE'
  | 'REPORTS_READ' | 'AUDIT_READ';

export interface SessionUser {
  id: number;
  name: string | null;
  email: string | null;
  role: string;
  permissions: Permission[];
  mustChangePassword: boolean;
}

export interface PageResult<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
}

export interface ApiErrorBody {
  error: {
    code: string;
    message: string;
    details: { field: string; message: string }[];
    requestId: string | null;
  };
}

export type OccupancyStatus = 'occupied' | 'vacant' | 'maintenance';

export interface OwnerSummary {
  id: number;
  fullName: string;
  documentMasked: string | null;
  email: string | null;
  phone: string | null;
  preferredRepassMethod: string | null;
  propertiesCount: number;
}

export interface PropertySummary {
  id: number;
  code: string;
  type: string;
  address: string;
  city: string;
  state: string;
  zipCode: string | null;
  rentCents: number;
  occupancyStatus: OccupancyStatus;
  ownerId: number | null;
  ownerName: string | null;
  tenantName: string | null;
}

export interface TenantSummary {
  id: number;
  fullName: string;
  documentMasked: string | null;
  email: string | null;
  phone: string | null;
  rentCents: number;
  propertyType: string | null;
  ownerNames: string[];
}
