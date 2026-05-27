-- ============================================================
--   HOME CARE MANAGEMENT SYSTEM
--   SQL Server Schema - Adapted & Improved from Group Schema
--   For Java Desktop Application (JDBC + SQL Server)
-- ============================================================

CREATE DATABASE HomeCare_Java_App;
GO
USE HomeCare_Java_App;
GO

-- ============================================================
-- TABLE 1: USERS (NEW - Required for Java Login System)
-- Handles authentication for Admin and Caregiver roles
-- ============================================================
CREATE TABLE Users (
    User_ID       VARCHAR(10)  NOT NULL,
    Username      VARCHAR(50)  NOT NULL UNIQUE,
    Password_Hash VARCHAR(255) NOT NULL,          -- Store hashed password (MD5 minimum)
    Role          VARCHAR(20)  NOT NULL,
    Is_Active     BIT          NOT NULL DEFAULT 1,
    Created_Date  DATETIME     NOT NULL DEFAULT GETDATE(),
    Last_Login    DATETIME     NULL,

    CONSTRAINT PK_Users PRIMARY KEY (User_ID),
    CONSTRAINT CHK_User_ID   CHECK (User_ID LIKE 'USR[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_User_Role CHECK (Role IN ('Admin', 'Caregiver'))
);
GO

-- ============================================================
-- TABLE 2: ADDRESS (Kept from original - minor improvements)
-- Shared lookup table used by Patients and Professionals
-- ============================================================
CREATE TABLE Address (
    Address_ID      VARCHAR(10)  NOT NULL,
    City            VARCHAR(50)  NOT NULL DEFAULT 'Addis Ababa',
    Sub_City        VARCHAR(50)  NOT NULL,
    Woreda          VARCHAR(50)  NOT NULL,
    Kebele_Number   INT          NOT NULL,
    House_Number    VARCHAR(20)  NOT NULL,
    Street_Name     VARCHAR(100) NULL,
    Landmark        VARCHAR(100) NULL,
    GPS_Coordinates VARCHAR(50)  NULL,
    Date_Added      DATE         NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    Is_Active       BIT          NOT NULL DEFAULT 1,

    CONSTRAINT PK_Address          PRIMARY KEY (Address_ID),
    CONSTRAINT CHK_Address_ID      CHECK (Address_ID LIKE 'ADD[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_Kebele_Number   CHECK (Kebele_Number BETWEEN 1 AND 99),
    CONSTRAINT CHK_Date_Added      CHECK (Date_Added >= '2023-01-01' AND Date_Added <= CAST(GETDATE() AS DATE))
);
GO

-- ============================================================
-- TABLE 3: DEMOGRAPHIC_INFO (Kept from original)
-- First contact information shared by Patients and Professionals
-- ============================================================
CREATE TABLE Demographic_Info (
    Demographic_ID    VARCHAR(10)  NOT NULL,
    Full_Name         VARCHAR(100) NOT NULL,
    Phone             VARCHAR(15)  NOT NULL,
    Email             VARCHAR(100) NULL,
    Age               INT          NULL,
    Gender            CHAR(1)      NULL,
    Marital_Status    VARCHAR(20)  NULL,
    Insurance_Provider VARCHAR(50) NULL,
    Referral_Source   VARCHAR(50)  NULL,
    Contact_Date      DATE         NOT NULL DEFAULT CAST(GETDATE() AS DATE),

    CONSTRAINT PK_Demographic_Info    PRIMARY KEY (Demographic_ID),
    CONSTRAINT CHK_Demographic_ID     CHECK (Demographic_ID LIKE 'DEM[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_Demographic_Phone  CHECK (Phone LIKE '+251%' AND LEN(Phone) >= 13),
    CONSTRAINT CHK_Demographic_Email  CHECK (Email IS NULL OR Email LIKE '%@%.%'),
    CONSTRAINT CHK_Age                CHECK (Age IS NULL OR Age BETWEEN 0 AND 120),
    CONSTRAINT CHK_Gender             CHECK (Gender IS NULL OR Gender IN ('M', 'F')),
    CONSTRAINT CHK_Marital_Status     CHECK (Marital_Status IS NULL OR Marital_Status IN ('Single','Married','Divorced','Widowed')),
    CONSTRAINT CHK_Referral_Source    CHECK (Referral_Source IS NULL OR Referral_Source IN ('Friend/Family','Doctor','Hospital','Advertisement','Online','Other')),
    CONSTRAINT CHK_Contact_Date       CHECK (Contact_Date >= '2020-01-01' AND Contact_Date <= CAST(GETDATE() AS DATE))
);
GO

-- ============================================================
-- TABLE 4: PATIENT (Kept from original - added User_ID link)
-- Registered patients receiving home care
-- ============================================================
CREATE TABLE Patient (
    Patient_ID              VARCHAR(10)  NOT NULL,
    Demographic_ID          VARCHAR(10)  NULL,
    Address_ID              VARCHAR(10)  NOT NULL,
    Emergency_Contact       VARCHAR(15)  NOT NULL,
    Emergency_Contact_Name  VARCHAR(100) NULL,
    Relationship            VARCHAR(50)  NULL,
    Insurance_Policy_No     VARCHAR(50)  NULL,
    Registration_Date       DATE         NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    Is_Active               BIT          NOT NULL DEFAULT 1,

    CONSTRAINT PK_Patient              PRIMARY KEY (Patient_ID),
    CONSTRAINT FK_Patient_Demographic  FOREIGN KEY (Demographic_ID) REFERENCES Demographic_Info(Demographic_ID),
    CONSTRAINT FK_Patient_Address      FOREIGN KEY (Address_ID)     REFERENCES Address(Address_ID),
    CONSTRAINT CHK_Patient_ID          CHECK (Patient_ID LIKE 'PAT[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_Emergency_Contact   CHECK (Emergency_Contact LIKE '+251%' AND LEN(Emergency_Contact) >= 13)
);
GO

-- ============================================================
-- TABLE 5: PATIENT_MEDICAL_HISTORY (Kept from original)
-- Full medical history per patient visit
-- ============================================================
CREATE TABLE Patient_Medical_History (
    History_ID            VARCHAR(10)  NOT NULL,
    Patient_ID            VARCHAR(10)  NOT NULL,
    Visit_Date            DATE         NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    Medical_Condition     VARCHAR(100) NOT NULL,
    Symptoms              VARCHAR(500) NULL,
    Diagnosis             VARCHAR(500) NULL,
    Treatment_Given       VARCHAR(500) NULL,
    Medication_Prescribed VARCHAR(500) NULL,
    Followup_Required     BIT          NOT NULL DEFAULT 0,
    Followup_Date         DATE         NULL,
    Next_Visit_Date       DATE         NULL,
    Status                VARCHAR(20)  NULL,
    Severity_Level        VARCHAR(20)  NULL,

    CONSTRAINT PK_Patient_Medical_History PRIMARY KEY (History_ID),
    CONSTRAINT FK_MedicalHistory_Patient  FOREIGN KEY (Patient_ID) REFERENCES Patient(Patient_ID),
    CONSTRAINT CHK_History_ID             CHECK (History_ID LIKE 'HIS[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_History_Status         CHECK (Status IS NULL OR Status IN ('Active','Resolved','Chronic','Monitoring')),
    CONSTRAINT CHK_Severity_Level         CHECK (Severity_Level IS NULL OR Severity_Level IN ('Low','Medium','High','Critical')),
    CONSTRAINT CHK_Followup_Date          CHECK (Followup_Date IS NULL OR Followup_Date >= Visit_Date)
);
GO

-- ============================================================
-- TABLE 6: HEALTHCARE_PROFESSIONAL (Kept - added User_ID link)
-- Doctors, nurses, caregivers, physiotherapists
-- ============================================================
CREATE TABLE Healthcare_Professional (
    Professional_ID  VARCHAR(10)    NOT NULL,
    User_ID          VARCHAR(10)    NULL,           -- Links to Users table for login
    Demographic_ID   VARCHAR(10)    NULL,
    Address_ID       VARCHAR(10)    NULL,
    Profession       VARCHAR(20)    NOT NULL,
    Specialization   VARCHAR(50)    NOT NULL,
    License_Number   VARCHAR(50)    NOT NULL UNIQUE,
    Years_Experience INT            NOT NULL,
    Hourly_Rate      DECIMAL(10,2)  NOT NULL,
    Is_Available     BIT            NOT NULL DEFAULT 1,
    Join_Date        DATE           NOT NULL DEFAULT CAST(GETDATE() AS DATE),

    CONSTRAINT PK_Healthcare_Professional PRIMARY KEY (Professional_ID),
    CONSTRAINT FK_Professional_User        FOREIGN KEY (User_ID)        REFERENCES Users(User_ID),
    CONSTRAINT FK_Professional_Demographic FOREIGN KEY (Demographic_ID) REFERENCES Demographic_Info(Demographic_ID),
    CONSTRAINT FK_Professional_Address     FOREIGN KEY (Address_ID)     REFERENCES Address(Address_ID),
    CONSTRAINT CHK_Professional_ID         CHECK (Professional_ID LIKE 'PRO[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_Profession              CHECK (Profession IN ('Doctor','Nurse','Caregiver','Physiotherapist')),
    CONSTRAINT CHK_Hourly_Rate             CHECK (Hourly_Rate > 0),
    CONSTRAINT CHK_Years_Experience        CHECK (Years_Experience >= 0)
);
GO

-- ============================================================
-- TABLE 7: SERVICE_CATEGORY (Kept from original)
-- Types of care services offered
-- ============================================================
CREATE TABLE Service_Category (
    Category_ID            VARCHAR(10)   NOT NULL,
    Category_Name          VARCHAR(50)   NOT NULL,
    Description            VARCHAR(255)  NOT NULL,
    Base_Rate              DECIMAL(10,2) NOT NULL,
    Duration_Required      INT           NOT NULL,
    Special_Equipment      VARCHAR(100)  NULL,
    Qualification_Required VARCHAR(50)   NOT NULL,
    Is_Active              BIT           NOT NULL DEFAULT 1,

    CONSTRAINT PK_Service_Category  PRIMARY KEY (Category_ID),
    CONSTRAINT CHK_Category_ID      CHECK (Category_ID LIKE 'CAT[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_Base_Rate        CHECK (Base_Rate > 0),
    CONSTRAINT CHK_Duration_Required CHECK (Duration_Required BETWEEN 1 AND 8)
);
GO

-- ============================================================
-- TABLE 8: APPOINTMENT (Core table - kept, minor fix)
-- Links patients to professionals for scheduled visits
-- ============================================================
CREATE TABLE Appointment (
    Appointment_ID       VARCHAR(10)  NOT NULL,
    Patient_ID           VARCHAR(10)  NOT NULL,
    Professional_ID      VARCHAR(10)  NOT NULL,
    Category_ID          VARCHAR(10)  NOT NULL,
    Appointment_Date     DATE         NOT NULL,
    Appointment_Time     TIME         NOT NULL,
    Created_Date         DATETIME     NOT NULL DEFAULT GETDATE(),
    Last_Modified        DATETIME     NULL,
    Cancelled_Date       DATETIME     NULL,
    Duration_Hours       INT          NOT NULL,
    Status               VARCHAR(20)  NOT NULL DEFAULT 'Scheduled',
    Special_Instructions VARCHAR(500) NULL,

    CONSTRAINT PK_Appointment             PRIMARY KEY (Appointment_ID),
    CONSTRAINT FK_Appointment_Patient     FOREIGN KEY (Patient_ID)      REFERENCES Patient(Patient_ID),
    CONSTRAINT FK_Appointment_Professional FOREIGN KEY (Professional_ID) REFERENCES Healthcare_Professional(Professional_ID),
    CONSTRAINT FK_Appointment_Category    FOREIGN KEY (Category_ID)     REFERENCES Service_Category(Category_ID),
    CONSTRAINT CHK_Appointment_ID         CHECK (Appointment_ID LIKE 'APT[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_Appointment_Status     CHECK (Status IN ('Scheduled','In Progress','Completed','Cancelled')),
    CONSTRAINT CHK_Duration_Hours         CHECK (Duration_Hours BETWEEN 1 AND 8),
    CONSTRAINT CHK_Created_Date           CHECK (Created_Date >= '2023-01-01' AND Created_Date <= GETDATE()),
    CONSTRAINT CHK_Last_Modified          CHECK (Last_Modified IS NULL OR (Last_Modified >= Created_Date AND Last_Modified <= GETDATE()))
);
GO

-- ============================================================
-- TABLE 9: SERVICE_FEEDBACK (Kept from original)
-- Patient satisfaction data per appointment
-- ============================================================
CREATE TABLE Service_Feedback (
    Feedback_ID          VARCHAR(10)  NOT NULL,
    Appointment_ID       VARCHAR(10)  NOT NULL UNIQUE,  -- One feedback per appointment
    Rating               INT          NOT NULL,
    Comments             VARCHAR(500) NULL,
    Feedback_Date        DATE         NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    Followup_Required    BIT          NOT NULL DEFAULT 0,
    Service_Satisfaction VARCHAR(20)  NULL,

    CONSTRAINT PK_Service_Feedback      PRIMARY KEY (Feedback_ID),
    CONSTRAINT FK_Feedback_Appointment  FOREIGN KEY (Appointment_ID) REFERENCES Appointment(Appointment_ID),
    CONSTRAINT CHK_Feedback_ID          CHECK (Feedback_ID LIKE 'FBK[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_Rating               CHECK (Rating BETWEEN 1 AND 5),
    CONSTRAINT CHK_Satisfaction         CHECK (Service_Satisfaction IS NULL OR Service_Satisfaction IN ('Excellent','Good','Average','Poor'))
);
GO

-- ============================================================
-- TABLE 10: PAYMENT (Kept from original)
-- Payment records linked to appointments
-- ============================================================
CREATE TABLE Payment (
    Payment_ID            VARCHAR(10)   NOT NULL,
    Appointment_ID        VARCHAR(10)   NOT NULL,
    Amount                DECIMAL(10,2) NOT NULL,
    Payment_Date          DATE          NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    Payment_Method        VARCHAR(20)   NOT NULL,
    Status                VARCHAR(20)   NOT NULL DEFAULT 'Pending',
    Transaction_Reference VARCHAR(50)   NULL,
    Receipt_Number        VARCHAR(50)   NULL,

    CONSTRAINT PK_Payment               PRIMARY KEY (Payment_ID),
    CONSTRAINT FK_Payment_Appointment   FOREIGN KEY (Appointment_ID) REFERENCES Appointment(Appointment_ID),
    CONSTRAINT UQ_Transaction_Reference UNIQUE (Transaction_Reference),
    CONSTRAINT UQ_Receipt_Number        UNIQUE (Receipt_Number),
    CONSTRAINT CHK_Payment_ID           CHECK (Payment_ID LIKE 'PAY[0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT CHK_Amount               CHECK (Amount > 0),
    CONSTRAINT CHK_Payment_Method       CHECK (Payment_Method IN ('Cash','Bank Transfer','Mobile Payment','Insurance')),
    CONSTRAINT CHK_Payment_Status       CHECK (Status IN ('Pending','Completed','Failed','Refunded'))
);
GO

-- ============================================================
-- VIEWS (Kept all 3 from original - they are excellent)
-- ============================================================

-- VIEW 1: Patient Summary Report
CREATE VIEW vw_Patient_Summary AS
SELECT
    p.Patient_ID,
    d.Full_Name         AS Patient_Name,
    d.Phone,
    d.Email,
    d.Age,
    d.Gender,
    d.Marital_Status,
    d.Insurance_Provider,
    a.Sub_City,
    a.Woreda,
    p.Emergency_Contact,
    p.Emergency_Contact_Name,
    p.Insurance_Policy_No,
    p.Registration_Date,
    p.Is_Active,
    (SELECT COUNT(*)   FROM Appointment WHERE Patient_ID = p.Patient_ID)                                AS Total_Appointments,
    (SELECT COUNT(*)   FROM Appointment WHERE Patient_ID = p.Patient_ID AND Status = 'Completed')       AS Completed_Appointments,
    (SELECT MAX(Appointment_Date) FROM Appointment WHERE Patient_ID = p.Patient_ID)                     AS Last_Appointment,
    (SELECT ISNULL(SUM(pay.Amount),0) FROM Payment pay
        JOIN Appointment apt ON pay.Appointment_ID = apt.Appointment_ID
        WHERE apt.Patient_ID = p.Patient_ID AND pay.Status = 'Completed')                               AS Total_Paid
FROM Patient p
JOIN Demographic_Info d ON p.Demographic_ID = d.Demographic_ID
JOIN Address a           ON p.Address_ID    = a.Address_ID;
GO

-- VIEW 2: Professional Performance Report
CREATE VIEW vw_Professional_Performance AS
SELECT
    hp.Professional_ID,
    d.Full_Name          AS Professional_Name,
    hp.Profession,
    hp.Specialization,
    hp.License_Number,
    hp.Years_Experience,
    hp.Hourly_Rate,
    hp.Is_Available,
    (SELECT COUNT(*) FROM Appointment WHERE Professional_ID = hp.Professional_ID)                                       AS Total_Appointments,
    (SELECT COUNT(*) FROM Appointment WHERE Professional_ID = hp.Professional_ID AND Status = 'Completed')              AS Completed,
    (SELECT COUNT(*) FROM Appointment WHERE Professional_ID = hp.Professional_ID AND Status = 'Cancelled')              AS Cancelled,
    (SELECT ISNULL(AVG(CAST(sf.Rating AS FLOAT)),0)
        FROM Appointment apt JOIN Service_Feedback sf ON apt.Appointment_ID = sf.Appointment_ID
        WHERE apt.Professional_ID = hp.Professional_ID)                                                                 AS Avg_Rating,
    (SELECT ISNULL(SUM(pay.Amount),0)
        FROM Appointment apt JOIN Payment pay ON apt.Appointment_ID = pay.Appointment_ID
        WHERE apt.Professional_ID = hp.Professional_ID AND pay.Status = 'Completed')                                    AS Revenue_Generated
FROM Healthcare_Professional hp
JOIN Demographic_Info d ON hp.Demographic_ID = d.Demographic_ID;
GO

-- VIEW 3: Appointment Detailed Report
CREATE VIEW vw_Appointment_Details AS
SELECT
    a.Appointment_ID,
    a.Appointment_Date,
    a.Appointment_Time,
    a.Duration_Hours,
    a.Status              AS Appointment_Status,
    a.Special_Instructions,
    pd.Full_Name          AS Patient_Name,
    pd.Phone              AS Patient_Phone,
    pd.Age                AS Patient_Age,
    hpd.Full_Name         AS Professional_Name,
    hp.Profession,
    sc.Category_Name      AS Service_Type,
    sc.Base_Rate,
    (hp.Hourly_Rate * a.Duration_Hours) AS Calculated_Cost,
    addr.Sub_City,
    addr.House_Number,
    addr.Landmark,
    sf.Rating,
    sf.Comments           AS Feedback,
    sf.Service_Satisfaction,
    pay.Amount,
    pay.Payment_Method,
    pay.Status            AS Payment_Status,
    pay.Receipt_Number
FROM Appointment a
JOIN Patient p                  ON a.Patient_ID      = p.Patient_ID
JOIN Demographic_Info pd        ON p.Demographic_ID  = pd.Demographic_ID
JOIN Healthcare_Professional hp ON a.Professional_ID = hp.Professional_ID
JOIN Demographic_Info hpd       ON hp.Demographic_ID = hpd.Demographic_ID
JOIN Service_Category sc        ON a.Category_ID     = sc.Category_ID
JOIN Address addr               ON p.Address_ID      = addr.Address_ID
LEFT JOIN Service_Feedback sf   ON a.Appointment_ID  = sf.Appointment_ID
LEFT JOIN Payment pay           ON a.Appointment_ID  = pay.Appointment_ID;
GO

-- ============================================================
-- STORED PROCEDURES (Kept originals + added 2 new ones for Java)
-- ============================================================

-- SP 1: Get Appointments By Date (original)
CREATE PROCEDURE sp_GetAppointmentsByDate
    @AppointmentDate DATE
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        a.Appointment_ID, a.Appointment_Time, a.Duration_Hours, a.Status,
        pd.Full_Name  AS Patient_Name,   pd.Phone AS Patient_Phone,
        hpd.Full_Name AS Professional_Name, hp.Profession,
        sc.Category_Name AS Service_Type
    FROM Appointment a
    JOIN Patient p                  ON a.Patient_ID      = p.Patient_ID
    JOIN Demographic_Info pd        ON p.Demographic_ID  = pd.Demographic_ID
    JOIN Healthcare_Professional hp ON a.Professional_ID = hp.Professional_ID
    JOIN Demographic_Info hpd       ON hp.Demographic_ID = hpd.Demographic_ID
    JOIN Service_Category sc        ON a.Category_ID     = sc.Category_ID
    WHERE a.Appointment_Date = @AppointmentDate
    ORDER BY a.Appointment_Time;
END
GO

-- SP 2: Get Patient Appointments (original)
CREATE PROCEDURE sp_GetPatientAppointments
    @PatientID VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT EXISTS (SELECT 1 FROM Patient WHERE Patient_ID = @PatientID)
    BEGIN
        PRINT 'Patient not found!';
        RETURN;
    END
    SELECT
        a.Appointment_ID, a.Appointment_Date, a.Appointment_Time,
        a.Duration_Hours, a.Status,
        hpd.Full_Name AS Professional_Name, hp.Profession,
        sc.Category_Name AS Service_Type,
        pay.Amount, pay.Status AS Payment_Status,
        sf.Rating, sf.Service_Satisfaction
    FROM Appointment a
    JOIN Healthcare_Professional hp ON a.Professional_ID = hp.Professional_ID
    JOIN Demographic_Info hpd       ON hp.Demographic_ID = hpd.Demographic_ID
    JOIN Service_Category sc        ON a.Category_ID     = sc.Category_ID
    LEFT JOIN Payment pay           ON a.Appointment_ID  = pay.Appointment_ID
    LEFT JOIN Service_Feedback sf   ON a.Appointment_ID  = sf.Appointment_ID
    WHERE a.Patient_ID = @PatientID
    ORDER BY a.Appointment_Date DESC;
END
GO

-- SP 3: NEW - Login Authentication (used by Java LoginFrame)
CREATE PROCEDURE sp_AuthenticateUser
    @Username VARCHAR(50),
    @PasswordHash VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        u.User_ID,
        u.Username,
        u.Role,
        hp.Professional_ID,
        d.Full_Name
    FROM Users u
    LEFT JOIN Healthcare_Professional hp ON u.User_ID = hp.User_ID
    LEFT JOIN Demographic_Info d         ON hp.Demographic_ID = d.Demographic_ID
    WHERE u.Username    = @Username
      AND u.Password_Hash = @PasswordHash
      AND u.Is_Active   = 1;

    -- Update last login time
    IF @@ROWCOUNT > 0
        UPDATE Users SET Last_Login = GETDATE() WHERE Username = @Username;
END
GO

-- SP 4: NEW - Dashboard Summary (used by Java DashboardFrame)
CREATE PROCEDURE sp_GetDashboardSummary
AS
BEGIN
    SET NOCOUNT ON;
    SELECT
        (SELECT COUNT(*) FROM Patient WHERE Is_Active = 1)                                          AS Total_Active_Patients,
        (SELECT COUNT(*) FROM Healthcare_Professional WHERE Is_Available = 1)                       AS Available_Professionals,
        (SELECT COUNT(*) FROM Appointment WHERE Appointment_Date = CAST(GETDATE() AS DATE))         AS Todays_Appointments,
        (SELECT COUNT(*) FROM Appointment WHERE Status = 'Scheduled'
            AND Appointment_Date >= CAST(GETDATE() AS DATE))                                        AS Upcoming_Appointments,
        (SELECT COUNT(*) FROM Appointment WHERE Status = 'Completed'
            AND Appointment_Date = CAST(GETDATE() AS DATE))                                         AS Completed_Today,
        (SELECT ISNULL(SUM(Amount),0) FROM Payment
            WHERE Payment_Date = CAST(GETDATE() AS DATE) AND Status = 'Completed')                  AS Revenue_Today;
END
GO

-- ============================================================
-- SAMPLE DATA (All from original + new Users table)
-- ============================================================

-- Users (NEW - Admin + Caregiver accounts for Java login)
INSERT INTO Users (User_ID, Username, Password_Hash, Role) VALUES
('USR00001', 'admin',    '21232f297a57a5a743894a0e4a801fc3', 'Admin'),     -- password: admin
('USR00002', 'jsmith',   '5f4dcc3b5aa765d61d8327deb882cf99', 'Caregiver'), -- password: password
('USR00003', 'btadesse', 'e10adc3949ba59abbe56e057f20f883e', 'Caregiver'), -- password: 123456
('USR00004', 'ygirma',   '25d55ad283aa400af464c76d713c07ad', 'Caregiver'); -- password: 12345678
GO

-- Address (original data)
INSERT INTO Address (Address_ID, City, Sub_City, Woreda, Kebele_Number, House_Number, Street_Name, Landmark, Date_Added, Is_Active) VALUES
('ADD00001','Addis Ababa','Bole','Woreda 03',17,'B-123','Africa Avenue','Near Bole International Airport','2023-01-15',1),
('ADD00002','Addis Ababa','Kirkos','Woreda 08',24,'K-456','Cameron Street','Behind ECA','2023-01-20',1),
('ADD00003','Addis Ababa','Lideta','Woreda 09',12,'L-789','Tewodros Street','Near Lideta Market','2023-02-01',1),
('ADD00004','Addis Ababa','Yeka','Woreda 11',31,'Y-234','Yeka Road','Next to Yeka Primary Hospital','2023-02-10',1),
('ADD00005','Addis Ababa','Nifas Silk','Woreda 19',8,'N-567','Ring Road','Opposite Getu Commercial','2023-02-15',1),
('ADD00006','Addis Ababa','Kolfe','Woreda 06',15,'K-890','Kolfe Road','Near Kolfe Mosque','2023-03-01',1),
('ADD00007','Addis Ababa','Gulele','Woreda 23',42,'G-123','Gulele Road','Close to Gulele Botanical Garden','2023-03-10',1),
('ADD00008','Addis Ababa','Arada','Woreda 01',5,'A-456','Piazza Street','Near Menelik Hospital','2023-03-20',1),
('ADD00009','Addis Ababa','Addis Ketema','Woreda 07',19,'AK-789','Addis Ketema Road','Behind Addis Ketema Bus Station','2023-04-01',1),
('ADD00010','Addis Ababa','Akaki','Woreda 14',27,'AK-234','Akaki Road','Near Akaki Bridge','2023-04-15',1),
('ADD00011','Addis Ababa','Bole','Woreda 04',22,'B-567','Senegal Street','Next to Edna Mall','2023-05-01',1),
('ADD00012','Addis Ababa','Kirkos','Woreda 07',11,'K-890','Gambia Street','Behind Shola Market','2023-05-10',1),
('ADD00013','Addis Ababa','Lemi Kura','Woreda 12',35,'LK-123','Lemi Road','Near Lemi Industrial Park','2023-06-01',1),
('ADD00014','Addis Ababa','Yeka','Woreda 09',16,'Y-456','Entoto Road','Opposite Entoto Park','2023-06-15',1),
('ADD00015','Addis Ababa','Bole','Woreda 02',9,'B-789','Ghana Street','Near Bambis Supermarket','2023-07-01',1);
GO

-- Demographic Info (original data)
INSERT INTO Demographic_Info (Demographic_ID,Full_Name,Phone,Email,Age,Gender,Marital_Status,Insurance_Provider,Referral_Source,Contact_Date) VALUES
('DEM00001','Abebe Kebede','+251911123456','ab@em.il',65,'M','Married','Ethiopian Insurance Corporation','Doctor','2023-01-10'),
('DEM00002','Tigist Haile','+251922234567','ti@em.il',72,'F','Widowed','Nyala Insurance','Hospital','2023-01-15'),
('DEM00003','Girma Tadese','+251933345678','gi@em.il',58,'M','Married','Lion Insurance','Friend/Family','2023-01-20'),
('DEM00004','Meron Alemu','+251944456789','me@em.il',45,'F','Divorced','United Insurance','Online','2023-02-01'),
('DEM00005','Tekle Berhan','+251955567890','te@em.il',80,'M','Widowed','Ethiopian Insurance Corporation','Doctor','2023-02-05'),
('DEM00006','Asnakech Wondimu','+251966678901','as@em.il',67,'F','Married','Nyala Insurance','Advertisement','2023-02-10'),
('DEM00007','Dawit Solomon','+251977789012','da@em.il',52,'M','Married','Lion Insurance','Other','2023-02-15'),
('DEM00008','Zeritu Bekele','+251988890123','ze@em.il',71,'F','Married','United Insurance','Friend/Family','2023-03-01'),
('DEM00009','Henok Ayele','+251999901234','he@em.il',48,'M','Single','Ethiopian Insurance Corporation','Hospital','2023-03-10'),
('DEM00010','Eyerusalem Fikre','+251910012345','ey@em.il',63,'F','Divorced','Nyala Insurance','Doctor','2023-03-15'),
('DEM00011','Mulugeta Desta','+251921123456','mu@em.il',55,'M','Married','Lion Insurance','Online','2023-04-01'),
('DEM00012','Tsehay Ayele','+251932234567','ts@em.il',69,'F','Widowed','United Insurance','Advertisement','2023-04-10'),
('DEM00013','Yonas Tadesse','+251943345678','yo@em.il',42,'M','Married','Ethiopian Insurance Corporation','Doctor','2023-04-20'),
('DEM00014','Birtukan Hailu','+251954456789','bi@em.il',77,'F','Widowed','Nyala Insurance','Friend/Family','2023-05-01'),
('DEM00015','Tsegaye Mamo','+251965567890','ts2@em.il',61,'M','Married','Lion Insurance','Hospital','2023-05-10'),
('DEM00016','Selam Tesfaye','+251976678901','se@em.il',35,'F','Married','United Insurance','Other','2023-06-01'),
('DEM00017','Berhanu Mekonnen','+251987789012','be@em.il',73,'M','Married','Ethiopian Insurance Corporation','Doctor','2023-06-10'),
('DEM00018','Askale Worku','+251998890123','aw@em.il',68,'F','Divorced','Nyala Insurance','Online','2023-06-20'),
('DEM00019','Kassahun Gebre','+251909901234','ka@em.il',70,'M','Widowed','Lion Insurance','Friend/Family','2023-07-01'),
('DEM00020','Meseret Tadesse','+251910112345','met@em.il',59,'F','Married','United Insurance','Advertisement','2023-07-05');
GO

-- Patients (original data)
INSERT INTO Patient (Patient_ID,Demographic_ID,Address_ID,Emergency_Contact,Emergency_Contact_Name,Relationship,Insurance_Policy_No,Registration_Date,Is_Active) VALUES
('PAT00001','DEM00001','ADD00001','+251911765432','Sara Abebe','Daughter','EIC-2023-001','2023-01-20',1),
('PAT00002','DEM00002','ADD00002','+251922876543','Mekonnen Haile','Son','NI-2023-002','2023-01-25',1),
('PAT00003','DEM00003','ADD00003','+251933987654','Almaz Girma','Spouse','LI-2023-003','2023-02-01',1),
('PAT00004','DEM00004','ADD00004','+251944098765','Chaltu Meron','Sister','UI-2023-004','2023-02-10',1),
('PAT00005','DEM00005','ADD00005','+251955109876','Lemma Tekle','Son','EIC-2023-005','2023-02-15',1),
('PAT00006','DEM00006','ADD00006','+251966210987','Worku Asnakech','Spouse','NI-2023-006','2023-02-20',1),
('PAT00007','DEM00007','ADD00007','+251977321098','Tigist Dawit','Daughter','LI-2023-007','2023-03-01',1),
('PAT00008','DEM00008','ADD00008','+251988432109','Alem Zeritu','Son','UI-2023-008','2023-03-10',1),
('PAT00009','DEM00009','ADD00009','+251999543210','Meron Henok','Sister','EIC-2023-009','2023-03-20',1),
('PAT00010','DEM00010','ADD00010','+251910654321','Yonas Eyerusalem','Brother','NI-2023-010','2023-04-01',1),
('PAT00011','DEM00011','ADD00011','+251921765432','Tsehay Mulugeta','Spouse','LI-2023-011','2023-04-15',1),
('PAT00012','DEM00012','ADD00012','+251932876543','Gashaw Tsehay','Son','UI-2023-012','2023-04-25',1);
GO

-- Healthcare Professionals (original + linked to Users)
INSERT INTO Healthcare_Professional (Professional_ID,User_ID,Demographic_ID,Address_ID,Profession,Specialization,License_Number,Years_Experience,Hourly_Rate,Is_Available,Join_Date) VALUES
('PRO00001','USR00002','DEM00013','ADD00013','Doctor','Geriatric Medicine','MED-2020-001',15,800.00,1,'2020-01-10'),
('PRO00002','USR00003','DEM00014','ADD00014','Nurse','Elderly Care','NRS-2019-002',8,350.00,1,'2021-03-15'),
('PRO00003','USR00004','DEM00015','ADD00015','Caregiver','Personal Care','CGR-2021-003',5,200.00,1,'2022-06-01'),
('PRO00004',NULL,'DEM00016','ADD00001','Physiotherapist','Rehabilitation','PHT-2018-004',10,500.00,1,'2019-02-20'),
('PRO00005',NULL,'DEM00017','ADD00002','Doctor','General Practice','MED-2022-005',7,600.00,1,'2022-08-01'),
('PRO00006',NULL,'DEM00018','ADD00003','Nurse','Wound Care','NRS-2020-006',6,300.00,1,'2021-11-10'),
('PRO00007',NULL,'DEM00019','ADD00004','Caregiver','Companionship','CGR-2023-007',3,180.00,1,'2023-01-15'),
('PRO00008',NULL,'DEM00020','ADD00005','Physiotherapist','Mobility Therapy','PHT-2021-008',5,450.00,1,'2022-04-05');
GO

-- Service Categories (original data)
INSERT INTO Service_Category (Category_ID,Category_Name,Description,Base_Rate,Duration_Required,Special_Equipment,Qualification_Required,Is_Active) VALUES
('CAT00001','Skilled Nursing','Professional nursing care including medication management and wound care',350.00,2,'Medical kit','Registered Nurse',1),
('CAT00002','Physiotherapy','Physical therapy and rehabilitation exercises',500.00,1,'Therapy equipment','Physiotherapist',1),
('CAT00003','Personal Care','Assistance with bathing, dressing, and grooming',200.00,3,'Personal care items','Certified Caregiver',1),
('CAT00004','Companionship','Social interaction and emotional support',180.00,4,'None','Caregiver',1),
('CAT00005','Medical Consultation','Doctor home visits for medical assessment',800.00,1,'Medical instruments','Medical Doctor',1),
('CAT00006','Medication Management','Medication reminders and administration',250.00,1,'Pill organizer','Nurse',1),
('CAT00007','Wound Care','Specialized wound dressing and care',300.00,1,'Wound care supplies','Wound Care Nurse',1),
('CAT00008','24-Hour Care','Round-the-clock care and monitoring',4500.00,5,'Monitoring equipment','Multiple professionals',1),
('CAT00009','Palliative Care','End-of-life comfort and support',400.00,4,'Comfort care items','Palliative Specialist',1),
('CAT00010','Post-Surgery Care','Recovery assistance after surgery',350.00,3,'Recovery aids','Post-op Specialist',1);
GO

-- Appointments (original data)
INSERT INTO Appointment (Appointment_ID,Patient_ID,Professional_ID,Category_ID,Appointment_Date,Appointment_Time,Created_Date,Last_Modified,Cancelled_Date,Duration_Hours,Status,Special_Instructions) VALUES
('APT00001','PAT00001','PRO00002','CAT00001','2023-06-10','10:00:00','2023-06-01 09:30:00',NULL,NULL,2,'Completed','Patient has high blood pressure, monitor carefully'),
('APT00002','PAT00002','PRO00001','CAT00005','2023-06-12','14:30:00','2023-06-02 11:15:00','2023-06-11 10:00:00',NULL,1,'Completed','Bring diabetes monitoring equipment'),
('APT00003','PAT00003','PRO00004','CAT00002','2023-06-15','09:00:00','2023-06-03 14:20:00',NULL,NULL,1,'Completed','Focus on knee rehabilitation exercises'),
('APT00004','PAT00004','PRO00003','CAT00003','2023-06-18','11:00:00','2023-06-05 10:45:00',NULL,NULL,3,'Completed','Assistance with bathing and dressing required'),
('APT00005','PAT00005','PRO00005','CAT00006','2023-06-20','15:00:00','2023-06-07 16:30:00',NULL,NULL,1,'Completed','New medication regimen, ensure understanding'),
('APT00006','PAT00006','PRO00006','CAT00007','2023-06-22','13:30:00','2023-06-08 09:15:00',NULL,NULL,1,'Completed','Diabetic foot ulcer dressing change'),
('APT00007','PAT00007','PRO00007','CAT00004','2023-06-25','16:00:00','2023-06-10 12:30:00',NULL,NULL,4,'Scheduled','Patient feeling lonely, needs companionship'),
('APT00008','PAT00008','PRO00002','CAT00006','2023-06-27','09:30:00','2023-06-12 08:45:00',NULL,NULL,1,'Scheduled','Organize weekly medication'),
('APT00009','PAT00009','PRO00004','CAT00002','2023-06-28','10:30:00','2023-06-13 11:20:00',NULL,NULL,1,'Scheduled','Post-stroke rehabilitation exercises'),
('APT00010','PAT00010','PRO00001','CAT00005','2023-06-30','11:30:00','2023-06-14 13:10:00',NULL,NULL,1,'Scheduled','Routine checkup for chronic conditions'),
('APT00011','PAT00001','PRO00002','CAT00001','2023-07-02','10:00:00','2023-06-15 09:30:00',NULL,NULL,2,'Scheduled','Follow-up on blood pressure monitoring'),
('APT00012','PAT00003','PRO00004','CAT00002','2023-07-05','09:00:00','2023-06-16 14:20:00',NULL,NULL,1,'Scheduled','Continue rehabilitation exercises'),
('APT00013','PAT00005','PRO00005','CAT00006','2023-07-08','15:00:00','2023-06-17 16:30:00',NULL,NULL,1,'Scheduled','Review medication adherence'),
('APT00014','PAT00002','PRO00003','CAT00003','2023-07-10','11:00:00','2023-06-18 10:45:00',NULL,NULL,3,'Cancelled','Patient requested cancellation due to family visit'),
('APT00015','PAT00011','PRO00008','CAT00002','2023-07-12','14:00:00','2023-06-19 11:15:00','2023-06-25 09:00:00',NULL,1,'Scheduled','New patient, initial assessment required');
GO

-- Medical History (original data)
INSERT INTO Patient_Medical_History (History_ID,Patient_ID,Visit_Date,Medical_Condition,Symptoms,Diagnosis,Treatment_Given,Medication_Prescribed,Followup_Required,Followup_Date,Next_Visit_Date,Status,Severity_Level) VALUES
('HIS00001','PAT00001','2023-01-25','Hypertension','Dizziness, headache','Stage 2 Hypertension','Prescribed medication','Lisinopril 10mg',1,'2023-02-25','2023-02-25','Monitoring','Medium'),
('HIS00002','PAT00001','2023-02-25','Hypertension','No symptoms, BP controlled','Controlled Hypertension','Continue medication','Lisinopril 10mg',1,'2023-03-25','2023-03-25','Monitoring','Low'),
('HIS00003','PAT00001','2023-03-25','Hypertension','Mild headache','BP slightly elevated','Adjusted dosage','Lisinopril 15mg',1,'2023-04-25','2023-04-25','Monitoring','Medium'),
('HIS00004','PAT00002','2023-02-01','Diabetes Type 2','Fatigue, frequent urination','Uncontrolled Diabetes','Insulin therapy','Insulin 20 units',1,'2023-03-01','2023-03-01','Active','High'),
('HIS00005','PAT00002','2023-03-01','Diabetes Type 2','Improved energy','Partially controlled','Continue insulin','Insulin 18 units',1,'2023-04-01','2023-04-01','Active','Medium'),
('HIS00006','PAT00003','2023-02-10','Osteoarthritis','Knee pain, stiffness','Moderate arthritis','Physical therapy','Ibuprofen as needed',1,'2023-03-10','2023-03-10','Chronic','Medium'),
('HIS00007','PAT00003','2023-03-10','Osteoarthritis','Improved mobility','Responding to therapy','Continue PT','Ibuprofen as needed',1,'2023-04-10','2023-04-10','Chronic','Low'),
('HIS00008','PAT00004','2023-02-20','Post-surgery recovery','Pain at incision site','Healing properly','Wound care, pain management','Paracetamol',1,'2023-03-05','2023-03-05','Resolved','Medium'),
('HIS00009','PAT00005','2023-03-01','Heart Disease','Chest pain, shortness of breath','Angina','Prescribed nitroglycerin','Nitroglycerin 0.4mg',1,'2023-04-01','2023-04-01','Active','Critical'),
('HIS00010','PAT00005','2023-04-01','Heart Disease','Reduced chest pain','Stable angina','Continue medication','Nitroglycerin 0.4mg',1,'2023-05-01','2023-05-01','Monitoring','High'),
('HIS00011','PAT00006','2023-03-10','Diabetic Foot Ulcer','Foot wound, redness','Infected ulcer','Wound cleaning, antibiotics','Antibiotics, insulin',1,'2023-03-24','2023-03-24','Active','High'),
('HIS00012','PAT00006','2023-03-24','Diabetic Foot Ulcer','Wound improving','Healing properly','Continue wound care','Continue antibiotics',1,'2023-04-07','2023-04-07','Active','Medium'),
('HIS00013','PAT00007','2023-04-01','Depression','Sadness, isolation','Moderate depression','Counseling, medication','Sertraline 50mg',1,'2023-05-01','2023-05-01','Active','Medium'),
('HIS00014','PAT00008','2023-04-10','Dementia','Memory loss, confusion','Early stage dementia','Cognitive therapy','Donepezil 5mg',1,'2023-05-10','2023-05-10','Chronic','High'),
('HIS00015','PAT00009','2023-04-20','Stroke Recovery','Left side weakness','Post-stroke paralysis','Physical therapy','Blood thinners',1,'2023-05-20','2023-05-20','Active','Critical'),
('HIS00016','PAT00010','2023-05-01','Arthritis','Joint pain, swelling','Rheumatoid arthritis','Anti-inflammatory medication','Prednisone',1,'2023-06-01','2023-06-01','Chronic','Medium');
GO

-- Feedback (original data)
INSERT INTO Service_Feedback (Feedback_ID,Appointment_ID,Rating,Comments,Feedback_Date,Followup_Required,Service_Satisfaction) VALUES
('FBK00001','APT00001',5,'Excellent nurse, very caring and professional','2023-06-11',0,'Excellent'),
('FBK00002','APT00002',4,'Doctor was knowledgeable but arrived late','2023-06-13',0,'Good'),
('FBK00003','APT00003',5,'Physiotherapist was great, very helpful exercises','2023-06-16',0,'Excellent'),
('FBK00004','APT00004',5,'Caregiver was patient and kind','2023-06-19',0,'Excellent'),
('FBK00005','APT00005',3,'Good service but communication could be better','2023-06-21',1,'Average'),
('FBK00006','APT00006',4,'Wound care was done properly, very satisfied','2023-06-23',0,'Good');
GO

-- Payments (original data)
INSERT INTO Payment (Payment_ID,Appointment_ID,Amount,Payment_Date,Payment_Method,Status,Transaction_Reference,Receipt_Number) VALUES
('PAY00001','APT00001',700.00,'2023-06-01','Mobile Payment','Completed','TRX-20230601-001','RCT-202306-001'),
('PAY00002','APT00002',800.00,'2023-06-02','Bank Transfer','Completed','TRX-20230602-002','RCT-202306-002'),
('PAY00003','APT00003',500.00,'2023-06-03','Cash','Completed','TRX-20345678-003','RCT-202306-003'),
('PAY00004','APT00004',600.00,'2023-06-05','Insurance','Completed','INS-CLAIM-004','RCT-202306-004'),
('PAY00005','APT00005',250.00,'2023-06-07','Mobile Payment','Completed','TRX-20230607-005','RCT-202306-005'),
('PAY00006','APT00006',300.00,'2023-06-08','Cash','Completed','TRX-27479824-006','RCT-202306-006'),
('PAY00007','APT00007',720.00,'2023-06-10','Bank Transfer','Pending','TRX-20230610-007','RCT-225364-007'),
('PAY00008','APT00008',250.00,'2023-06-12','Mobile Payment','Pending','TRX-20230612-008','RCT-243579-008'),
('PAY00009','APT00009',500.00,'2023-06-13','Insurance','Completed','INS-CLAIM-009','RCT-202306-009'),
('PAY00010','APT00010',800.00,'2023-06-14','Cash','Completed','TRX-20876743-010','RCT-202306-010'),
('PAY00011','APT00011',700.00,'2023-06-15','Mobile Payment','Pending','TRX-20230615-011','RCT-786543-011'),
('PAY00012','APT00012',500.00,'2023-06-16','Bank Transfer','Completed','TRX-20230616-012','RCT-202306-012');
GO

-- ============================================================
-- VERIFY EVERYTHING LOADED CORRECTLY
-- ============================================================
SELECT 'Users'                    AS TableName, COUNT(*) AS Rows FROM Users
UNION ALL SELECT 'Address',                COUNT(*) FROM Address
UNION ALL SELECT 'Demographic_Info',       COUNT(*) FROM Demographic_Info
UNION ALL SELECT 'Patient',                COUNT(*) FROM Patient
UNION ALL SELECT 'Healthcare_Professional',COUNT(*) FROM Healthcare_Professional
UNION ALL SELECT 'Service_Category',       COUNT(*) FROM Service_Category
UNION ALL SELECT 'Appointment',            COUNT(*) FROM Appointment
UNION ALL SELECT 'Patient_Medical_History',COUNT(*) FROM Patient_Medical_History
UNION ALL SELECT 'Service_Feedback',       COUNT(*) FROM Service_Feedback
UNION ALL SELECT 'Payment',                COUNT(*) FROM Payment;
GO
