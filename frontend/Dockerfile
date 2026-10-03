# ---- Build stage: compile the React app to static files ----
FROM node:22-alpine AS build
WORKDIR /app

# Install dependencies first (cached unless package*.json changes)
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ---- Runtime stage: nginx serves the static files and proxies /api ----
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
