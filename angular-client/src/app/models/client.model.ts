export interface Client {
  id?: number;
  personType: PersonType;
  cpf?: string;
  cnpj?: string;
  name?: string;
  rg?: string;
  birthDate?: Date;
  companyName?: string;
  stateRegistration?: string;
  foundationDate?: Date;
  email: string;
  active: boolean;
  addresses: Address[];
}

export interface Address {
  id?: number;
  street: string;
  number: string;
  zipCode: string;
  neighborhood: string;
  phone: string;
  city: string;
  state: string;
  mainAddress: boolean;
  complement?: string;
}

export enum PersonType {
  INDIVIDUAL = 'INDIVIDUAL',
  COMPANY = 'COMPANY'
}