function fn() {
    var env = karate.env || 'test';
    var config = {
        env: env,
        baseUrl: 'http://localhost:8080', // porta del backend-api in test

        // i token
        // userAuth: 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1dGVudGUgZ2VuZXJpY28iLCJyb2xlcyI6WyJVU0VSIl0sImV4cCI6MTUxNjIzOTAyMn0.Gw8gxGoKN7bC2qMkbwS5k-38PffjMFvb0At69bV8E00',
        // curatorAuth: 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjdXJhdG9yZSIsInJvbGVzIjpbIlVTRVIiLCJDVVJBVE9SIl0sImV4cCI6MTUxNjIzOTAyMn0.VHpR0rbaRZHsVLUmvO7dKvojYHXTGfSYPuLbpQ26fjc'
        userAuth: 'Bearer dXRlbnRlIGdlbmVyaWNvfDN8VVNFUnwxMjM0NTYK',
        curatorAuth: 'Bearer Y3VyYXRvcmV8MnxVU0VSLENVUkFUT1IsUk9MRV9DVVJBVE9SfDEyMzQ1Ngo='
    };

    // Header comuni
    karate.configure('headers', { 'Content-Type': 'application/json' });
    karate.configure('retry', { count: 10, interval: 1000 }); // 10 tentativi x 1s = 10s

    return config;
}
