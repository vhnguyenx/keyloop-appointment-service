-- ==========================================
-- Drop existing tables if they exist (Clean Setup)
-- ==========================================
IF OBJECT_ID('dbo.AppointmentItem', 'U') IS NOT NULL DROP TABLE dbo.AppointmentItem;
IF OBJECT_ID('dbo.Appointment', 'U') IS NOT NULL DROP TABLE dbo.Appointment;
IF OBJECT_ID('dbo.TechnicianSkill', 'U') IS NOT NULL DROP TABLE dbo.TechnicianSkill;
IF OBJECT_ID('dbo.Technician', 'U') IS NOT NULL DROP TABLE dbo.Technician;
IF OBJECT_ID('dbo.ServiceBay', 'U') IS NOT NULL DROP TABLE dbo.ServiceBay;
IF OBJECT_ID('dbo.Vehicle', 'U') IS NOT NULL DROP TABLE dbo.Vehicle;
IF OBJECT_ID('dbo.Customer', 'U') IS NOT NULL DROP TABLE dbo.Customer;
IF OBJECT_ID('dbo.ServiceType', 'U') IS NOT NULL DROP TABLE dbo.ServiceType;
IF OBJECT_ID('dbo.Dealership', 'U') IS NOT NULL DROP TABLE dbo.Dealership;

-- ==========================================
-- Table Creations
-- ==========================================

CREATE TABLE Customer (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    fullName NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20),
    email NVARCHAR(255)
);

CREATE TABLE Vehicle (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    customerId UNIQUEIDENTIFIER NOT NULL,
    vin NVARCHAR(50),
    make NVARCHAR(50),
    model NVARCHAR(50),
    year INT,
    CONSTRAINT FK_Vehicle_Customer FOREIGN KEY (customerId) REFERENCES Customer(id)
);

CREATE TABLE Dealership (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(255) NOT NULL,
    address NVARCHAR(MAX)
);

CREATE TABLE ServiceBay (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    dealershipId UNIQUEIDENTIFIER NOT NULL,
    name NVARCHAR(100),
    CONSTRAINT FK_ServiceBay_Dealership FOREIGN KEY (dealershipId) REFERENCES Dealership(id)
);

CREATE TABLE ServiceType (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(255) NOT NULL,
    estimatedDuration INT,
    description NVARCHAR(MAX)
);

CREATE TABLE Technician (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    dealershipId UNIQUEIDENTIFIER NOT NULL,
    fullName NVARCHAR(255) NOT NULL,
    CONSTRAINT FK_Technician_Dealership FOREIGN KEY (dealershipId) REFERENCES Dealership(id)
);

CREATE TABLE TechnicianSkill (
    technicianId UNIQUEIDENTIFIER NOT NULL,
    serviceTypeId UNIQUEIDENTIFIER NOT NULL,
    PRIMARY KEY (technicianId, serviceTypeId),
    CONSTRAINT FK_TechnicianSkill_Technician FOREIGN KEY (technicianId) REFERENCES Technician(id),
    CONSTRAINT FK_TechnicianSkill_ServiceType FOREIGN KEY (serviceTypeId) REFERENCES ServiceType(id)
);

CREATE TABLE Appointment (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    customerId UNIQUEIDENTIFIER NOT NULL,
    vehicleId UNIQUEIDENTIFIER NOT NULL,
    dealershipId UNIQUEIDENTIFIER NOT NULL,
    technicianId UNIQUEIDENTIFIER,
    serviceBayId UNIQUEIDENTIFIER,
    startTime DATETIME2,
    endTime DATETIME2,
    totalEstimatedDuration INT,
    status NVARCHAR(50),
    createdAt DATETIME2 DEFAULT GETDATE(),
    updatedAt DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_Appointment_Customer FOREIGN KEY (customerId) REFERENCES Customer(id),
    CONSTRAINT FK_Appointment_Vehicle FOREIGN KEY (vehicleId) REFERENCES Vehicle(id),
    CONSTRAINT FK_Appointment_Dealership FOREIGN KEY (dealershipId) REFERENCES Dealership(id),
    CONSTRAINT FK_Appointment_Technician FOREIGN KEY (technicianId) REFERENCES Technician(id),
    CONSTRAINT FK_Appointment_ServiceBay FOREIGN KEY (serviceBayId) REFERENCES ServiceBay(id)
);

CREATE TABLE AppointmentItem (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    appointmentId UNIQUEIDENTIFIER NOT NULL,
    serviceTypeId UNIQUEIDENTIFIER NOT NULL,
    estimatedDuration INT,
    sequence INT,
    CONSTRAINT FK_AppointmentItem_Appointment FOREIGN KEY (appointmentId) REFERENCES Appointment(id),
    CONSTRAINT FK_AppointmentItem_ServiceType FOREIGN KEY (serviceTypeId) REFERENCES ServiceType(id)
);
