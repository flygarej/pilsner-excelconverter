* Mermaid available in GitHub

Testing if mermaid works as stated in github markdown:

```mermaid 
flowchart TD
    A[Deploy to production] -->{Is it Friday?};
    B --> Yes --> C[Do not deplot!];
    B --> No --> D[Run deploy.sh to deploy!];
    C ----> E[Enjoy your weekend!];
    D ----> E[Enjoy your weekend!];
```


