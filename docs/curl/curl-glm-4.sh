curl -X POST "https://open.bigmodel.cn/api/paas/v4/chat/completions" \
-H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiZDM3YzRmNTAwYjUwNGFkZGJiNWUyNjVmYzkzM2RhYTAiLCJleHAiOjE3NzI3Njc1Nzc0MTIsInRpbWVzdGFtcCI6MTc3Mjc2NTc3NzQxNn0.tl0MzN72pHioh573pe7iit4NjJRqjzeW4YAb1nNNaos" \
-H "Content-Type: application/json" \
-d @payload.json

