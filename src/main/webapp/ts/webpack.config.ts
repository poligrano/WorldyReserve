
// @ts-ignore
import path from "node:path";
// @ts-ignore
import { fileURLToPath } from "url";
import webpack from "webpack";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const config: webpack.Configuration = {
    entry: {
        Utils: "./src/Utils/index.ts",
        map: "./src/map/index.ts"
    },
    mode: "production",
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: "ts-loader",
                exclude: /node_modules/,
            },
        ],
    },
    resolve: {
        extensions: [".tsx", ".ts", ".js"],
    },
    output: {
        library: "[name]",
        filename: "[name].js",
        path: path.resolve(__dirname, "dist"),
    },
};

export default config;