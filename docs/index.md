# Documentation Index

This project includes comprehensive documentation organized by topic.

## Getting Started

Start here if you're new to the project:

1. **[README.md](../README.md)** - Main project documentation with quick start guide
2. **[OPENAPI-QUICKSTART.md](../OPENAPI-QUICKSTART.md)** - Quick reference for testing the API

## API Documentation

- **[API-DOCUMENTATION.md](../API-DOCUMENTATION.md)** - Complete API reference with examples
- **[OPENAPI-QUICKSTART.md](../OPENAPI-QUICKSTART.md)** - Quick reference for OpenAPI/Swagger
- **OpenAPI Spec** - Located at `server/src/main/resources/openapi/documentation.yaml`

## Development

- **[CLI-README.md](../CLI-README.md)** - Development CLI tools documentation  
- **[CLI-SUMMARY.md](../CLI-SUMMARY.md)** - Summary of CLI tools and features

## Deployment & Infrastructure

- **[DOCKER-README.md](../DOCKER-README.md)** - Docker and CI/CD setup guide
- **[SETUP-SUMMARY.md](../SETUP-SUMMARY.md)** - Setup completion summary with checklist
- **[scripts/db/README.md](../scripts/db/README.md)** - Database management guide

## Project Requirements

- **[Practical exercise 1.pdf](./Practical%20exercise%201.pdf)** - Original exercise requirements
- **[Kotlin Demo Kick Off.md](./Kotlin%20Demo%20Kick%20Off.md)** - Initial project kick-off notes

## Document Purposes

### Main Documentation
| Document | Purpose | Audience |
|----------|---------|----------|
| README.md | Main project overview, quick start | Everyone |
| API-DOCUMENTATION.md | Complete API reference | API consumers, frontend developers |
| OPENAPI-QUICKSTART.md | Quick API testing guide | Developers, QA |

### Development & Tools
| Document | Purpose | Audience |
|----------|---------|----------|
| CLI-README.md | Development CLI tools usage | Developers |
| CLI-SUMMARY.md | CLI tools feature summary | Developers |

### Deployment & Operations
| Document | Purpose | Audience |
|----------|---------|----------|
| DOCKER-README.md | Docker setup and deployment | DevOps, Developers |
| SETUP-SUMMARY.md | Infrastructure setup summary | DevOps |
| scripts/db/README.md | Database management | DevOps, Developers |

## Quick Links

### For Developers
1. **Getting Started**: [README.md](../README.md#quick-start)
2. **Run Tests**: `./dev test` or see [CLI-README.md](../CLI-README.md#testing--quality)
3. **API Documentation**: [API-DOCUMENTATION.md](../API-DOCUMENTATION.md)
4. **Development CLI**: [CLI-README.md](../CLI-README.md)

### For DevOps / Deployment
1. **Docker Setup**: [DOCKER-README.md](../DOCKER-README.md)
2. **Database Management**: [scripts/db/README.md](../scripts/db/README.md)
3. **Deployment Package**: [DOCKER-README.md#deployment-package](../DOCKER-README.md#-deployment-package)
4. **CI/CD Pipeline**: [DOCKER-README.md#-cicd-pipeline](../DOCKER-README.md#-cicd-pipeline)

### For API Consumers
1. **Quick Start**: [OPENAPI-QUICKSTART.md](../OPENAPI-QUICKSTART.md)
2. **API Reference**: [API-DOCUMENTATION.md](../API-DOCUMENTATION.md)
3. **Swagger UI**: http://localhost:8080/swagger (when running)
4. **OpenAPI Spec**: http://localhost:8080/openapi (when running)

## File Organization

```
docs/
├── index.md                          # This file - documentation index
├── Practical exercise 1.pdf          # Original requirements
├── Kotlin Demo Kick Off.md           # Project kick-off notes
└── Originial-ktor-docs.md            # Original Ktor documentation

# Root level documentation
├── README.md                          # Main project documentation
├── API-DOCUMENTATION.md               # Complete API reference
├── OPENAPI-QUICKSTART.md              # OpenAPI quick reference
├── CLI-README.md                      # CLI tools documentation
├── CLI-SUMMARY.md                     # CLI tools summary
├── DOCKER-README.md                   # Docker & deployment guide
└── SETUP-SUMMARY.md                   # Setup summary

# Scripts documentation
└── scripts/db/README.md               # Database management guide
```

## Documentation Maintenance

When updating documentation:

1. **README.md** - Update for major features, setup changes, or API changes
2. **API-DOCUMENTATION.md** - Update when endpoints change
3. **OPENAPI Spec** - Update `server/src/main/resources/openapi/documentation.yaml`
4. **CLI-README.md** - Update when adding new CLI commands
5. **DOCKER-README.md** - Update for deployment or infrastructure changes

## Contributing to Documentation

- Keep documentation concise and up-to-date
- Include code examples where helpful
- Cross-reference related documents
- Update this index when adding new documents
- Follow Markdown best practices

---

Last updated: May 23, 2026
