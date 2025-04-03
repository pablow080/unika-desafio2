import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { Client, PersonType } from '../../models/client.model';
import { ClientService } from '../../services/client.service';
import { ReportService } from '../../services/report.service';
import { NotificationService } from '../../services/notification.service';
import { ClientFormComponent } from '../client-form/client-form.component';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.scss']
})
export class ClientListComponent implements OnInit {
  displayedColumns: string[] = ['type', 'name', 'document', 'email', 'status', 'actions'];
  dataSource: MatTableDataSource<Client>;
  loading = false;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(
    private clientService: ClientService,
    private reportService: ReportService,
    private notificationService: NotificationService,
    private dialog: MatDialog
  ) {
    this.dataSource = new MatTableDataSource();
  }

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.loading = true;
    this.clientService.getClients().subscribe({
      next: (clients) => {
        this.dataSource.data = clients;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
      },
      error: (error) => {
        this.notificationService.error('Error loading clients');
        console.error(error);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  openClientDialog(client?: Client): void {
    const dialogRef = this.dialog.open(ClientFormComponent, {
      width: '800px',
      data: client || { personType: PersonType.INDIVIDUAL, active: true, addresses: [] }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadClients();
      }
    });
  }

  deleteClient(client: Client): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirm Delete',
        message: 'Are you sure you want to delete this client? This action cannot be undone.'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.clientService.deleteClient(client.id).subscribe({
          next: () => {
            this.notificationService.success('Client deleted successfully');
            this.loadClients();
          },
          error: (error) => {
            this.notificationService.error('Error deleting client');
            console.error(error);
          }
        });
      }
    });
  }

  generatePdf(client: Client): void {
    this.reportService.generatePdfReport(client.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url);
      },
      error: (error) => {
        this.notificationService.error('Error generating PDF report');
        console.error(error);
      }
    });
  }

  generateExcel(): void {
    this.reportService.generateExcelReport().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'clients.xlsx';
        link.click();
      },
      error: (error) => {
        this.notificationService.error('Error generating Excel report');
        console.error(error);
      }
    });
  }

  importExcel(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.reportService.importFromExcel(file).subscribe({
        next: () => {
          this.notificationService.success('Clients imported successfully');
          this.loadClients();
        },
        error: (error) => {
          this.notificationService.error('Error importing clients');
          console.error(error);
        }
      });
    }
  }

  getDisplayName(client: Client): string {
    return client.personType === PersonType.INDIVIDUAL ? client.name : client.companyName;
  }

  getDocument(client: Client): string {
    return client.personType === PersonType.INDIVIDUAL ? client.cpf : client.cnpj;
  }
}