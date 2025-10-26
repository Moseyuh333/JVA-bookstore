## Pending Tasks

- Deploy to Heroku and test end-to-end OTP + reset flows
- Verify EmailJS delivery in production (check spam if Gmail)

## EmailJS configuration (new)

Backend now supports EmailJS as primary email sender. Set these environment variables (Heroku Config Vars or local):

- EMAILJS_SERVICE_ID=service_ssjp9md
- EMAILJS_PUBLIC_KEY=F34pTxmNdUjckT9-o
- EMAILJS_TEMPLATE_REGISTER=template_pf8qw9d
- EMAILJS_TEMPLATE_RESET=template_sjv9tjr
- EMAILJS_FROM_NAME=Nkbookstore

If EmailJS variables are missing, system falls back to SMTP (MailerToGo) using SMTP_* envs or `email.properties`.
