import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Client, PersonType } from '../../models/client.model';
import { ClientService } from '../../services/client.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-client-form',
  templateUrl: './client-form.component.html',
  styleUrls: ['./client-form.component.scss']
})
export class ClientFormComponent {
  form: FormGroup;
  personTypes = Object.values(PersonType);
  loading = false;

  constructor(
    private fb: FormBuilder,
    private clientService: ClientService,
    private notificationService: NotificationService,
    private dialogRef: MatDialogRef<ClientFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Client
  ) {
    this.createForm();
  }

  createForm(): void {
    this.form = this.fb.group({
      id: [this.data.id],
      personType: [this.data.personType, Validators.required],
      cpf: [this.data.cpf],
      cnpj: [this.data.cnpj],
      name: [this.data.name],
      rg: [this.data.rg],
      birthDate: [this.data.birthDate],
      companyName: [this.data.companyName],
      stateRegistration: [this.data.stateRegistration],
      foundationDate: [this.data.foundationDate],
      email: [this.data.email, [Validators.required, Validators.email]],
      active: [this.data.active],
      addresses: this.fb.array(this.data.addresses.map(address => this.createAddressGroup(address)))
    });

    this.form.get('personType').valueChanges.subscribe(value => {
      this.updateValidators(value);
    });

    this.updateValidators(this.data.personType);
  }

  createAddressGroup(address: any = {}): FormGroup {
    return this.fb.group({
      id: [address.id],
      street: [address.street, Validators.required],
      number: [address.number, Validators.required],
      zipCode: [address.zipCode, Validators.required],
      neighborhood: [address.neighborhood, Validators.required],
      phone: [address.phone, Validators.required],
      city: [address.city, Validators.required],
      state: [address.state, Validators.required],
      mainAddress: [address.mainAddress],
      complement: [address.complement]
    });
  }

  updateValidators(personType: PersonType): void {
    const cpfControl = this.form.get('cpf');
    const nameControl = this.form.get('name');
    const cnpjControl = this.form.get('cnpj');
    const companyNameControl = this.form.get('companyName');

    if (personType === PersonType.INDIVIDUAL) {
      cpfControl.setValidators([Validators.required, Validators.minLength(11)]);
      nameControl.setValidators([Validators.required]);
      cnpjControl.clearValidators();
      companyNameControl.clearValidators();
    } else {
      cpfControl.clearValidators();
      nameControl.clearValidators();
      cnpjControl.setValidators([Validators.required, Validators.minLength(14)]);
      companyNameControl.setValidators([Validators.required]);
    }

    cpfControl.updateValueAndValidity();
    nameControl.updateValueAndValidity();
    cnpjControl.updateValueAndValidity();
    companyNameControl.updateValueAndValidity();
  }

  addAddress(): void {
    const addresses = this.form.get('addresses') as FormArray;
    addresses.push(this.createAddressGroup());
  }

  removeAddress(index: number): void {
    const addresses = this.form.get('addresses') as FormArray;
    addresses.removeAt(index);
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.loading = true;
      const client = this.form.value;

      const operation = client.id ?
        this.clientService.updateClient(client) :
        this.clientService.createClient(client);

      operation.subscribe({
        next: () => {
          this.notificationService.success(`Client ${client.id ? 'updated' : 'created'} successfully`);
          this.dialogRef.close(true);
        },
        error: (error) => {
          this.notificationService.error(`Error ${client.id ? 'updating' : 'creating'} client`);
          console.error(error);
          this.loading = false;
        }
      });
    }
  }
}