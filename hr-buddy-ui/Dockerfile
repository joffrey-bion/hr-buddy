#--------------
# Builder image

FROM node:10.10.0-alpine as builder

WORKDIR /usr/src/app

# running install only first to avoid rebuilding image if deps didn't change
COPY package*.json ./
RUN npm install

# building the rest of the app
COPY . .
RUN npm run build


#-----------------
# Production image

FROM nginx:1.13.12-alpine
LABEL maintainer="joffrey.bion@gmail.com"

COPY --from=builder /usr/src/app/dist/hr-buddy-ui /usr/share/nginx/html
EXPOSE 80

# The entry point is a wrapper that generates the JSON config from environment variables
COPY ./entryPoint.sh /
RUN chmod +x entryPoint.sh
ENTRYPOINT ["/entryPoint.sh"]

CMD ["nginx", "-g", "daemon off;"]
