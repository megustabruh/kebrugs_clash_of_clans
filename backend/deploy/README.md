# AWS EC2 Deployment Guide

## Prerequisites

1. AWS EC2 instance (Amazon Linux 2 or Ubuntu)
2. MySQL database (RDS recommended)
3. Clash of Clans API token from https://developer.clashofclans.com/

## Important: API Token IP Whitelisting

The CoC API token is IP-restricted. You must:
1. Get your EC2 instance's **public IP**
2. Generate a new API token at https://developer.clashofclans.com/ with that IP

## Deployment Steps

### 1. Build the JAR locally

```bash
cd backend
mvn clean package -DskipTests
```
or 
./run.sh

### 2. Copy files to EC2

```bash
# Replace with your EC2 details
EC2_HOST=ec2-user@your-ec2-ip

scp -i your-key.pem target/clash-of-clans-backend-1.0.0.jar $EC2_HOST:~/
scp -i your-key.pem -r deploy/ $EC2_HOST:~/
```

### 3. SSH into EC2 and deploy

```bash
ssh -i your-key.pem $EC2_HOST

# Run deployment script
chmod +x deploy/deploy.sh
./deploy/deploy.sh
```

### 4. Configure environment variables

```bash
sudo nano /opt/coc-backend/.env
```

Update these values:
```
DB_URL=jdbc:mysql://database-1.c326eq2kmmi5.ap-southeast-2.rds.amazonaws.com:3306/personal?sslMode=VERIFY_IDENTITY
DB_USERNAME=admin
DB_PASSWORD=<your RDS password>
COC_API_TOKEN=your_new_token_with_ec2_ip
```

`personal` is the database name used by the current application configuration; replace it in `DB_URL` if the target database has a different name. Do not commit the RDS password. The `sslMode=VERIFY_IDENTITY` setting encrypts the connection and verifies the RDS endpoint hostname. The MySQL CLI command can be used to test access from the EC2 instance; keep the AWS CA bundle available for that test:

```bash
mysql -h database-1.c326eq2kmmi5.ap-southeast-2.rds.amazonaws.com \
  -P 3306 -u admin -p \
  --ssl-mode=VERIFY_IDENTITY --ssl-ca=./global-bundle.pem
```

The Java runtime must trust the Amazon RDS CA for the JDBC TLS verification to succeed. If the service reports a certificate trust error, configure the JVM truststore with the relevant AWS RDS CA certificate before restarting the service.

### 5. Start the service

```bash
sudo systemctl start coc-backend
sudo systemctl status coc-backend
```

## Useful Commands

```bash
# View logs
sudo journalctl -u coc-backend -f

# Restart service
sudo systemctl restart coc-backend

# Stop service
sudo systemctl stop coc-backend

# Check if running
sudo systemctl status coc-backend
```

## Scheduling

The application automatically:
- Fetches war data on startup
- Runs every 1 hour via `@Scheduled` annotation
- Creates/updates database tables automatically (`ddl-auto=update`)

## Database Tables Created

| Table | Description |
|-------|-------------|
| `players` | Clan members |
| `wars` | War records |
| `attacks` | Attack details per war |

## Security Group Settings

Ensure your EC2 security group allows:
- Inbound: Port 8081 (or your SERVER_PORT)
- Outbound: Port 443 (for CoC API)
- Outbound: Port 3306 (for MySQL/RDS)

## Troubleshooting

### 403 Forbidden from CoC API
- Your API token is not whitelisted for EC2's IP
- Generate a new token with EC2's public IP

### Database connection failed
- `Access denied for user 'root'@'<EC2 address>'` means MySQL was reached but rejected authentication. The later Hibernate dialect error is a consequence of that failed connection.
- Check `/opt/coc-backend/.env` and set `DB_USERNAME` and `DB_PASSWORD` to the RDS application user's credentials (the deployment example uses `admin`); do not use the MySQL `root` account.
- Check security group allows EC2 → RDS on port 3306 and verify the configured user is allowed to connect from the EC2 host.
- Verify credentials interactively without putting the password in shell history:

  ```bash
  mysql -h <RDS endpoint> -P 3306 -u <DB_USERNAME> -p \
    --ssl-mode=VERIFY_IDENTITY --ssl-ca=./global-bundle.pem
  ```

- Restart the service after correcting `.env`: `sudo systemctl restart coc-backend`.
- The deployment script preserves an existing `.env`; it now stops with an actionable error if `DB_USERNAME=root` or `DB_PASSWORD` is empty.

### Service won't start
- Check logs: `sudo journalctl -u coc-backend -n 100`
- Verify Java is installed: `java -version`
